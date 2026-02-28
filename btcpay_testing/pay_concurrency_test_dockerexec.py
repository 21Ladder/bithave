#!/usr/bin/env python3
# Pay a batch of LN invoices via lncli in a docker container and log results.
import argparse, csv, json, os, subprocess, time, uuid
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
from statistics import mean, median

def utc_now_iso():
    # Current UTC time as ISO string.
    return datetime.now(timezone.utc).isoformat()

def run_cmd(cmd, timeout=None):
    # Run a subprocess and capture output plus duration.
    t0 = time.time()
    p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
    dt_ms = int(round((time.time() - t0) * 1000))
    return p.returncode, p.stdout, p.stderr, dt_ms

def safe_json(s):
    # Best-effort JSON parse, return None on failure.
    try:
        return json.loads(s)
    except Exception:
        return None

def ensure_lncli(container, network):
    # Quick sanity check that lncli works inside the container.
    rc, out, err, _ = run_cmd(["docker", "exec", container, "lncli", f"--network={network}", "getinfo"], timeout=10)
    if rc != 0:
        raise RuntimeError(f"lncli getinfo failed via docker exec.\nSTDERR:\n{err}\nSTDOUT:\n{out}")

def load_invoices(path):
    # Load invoice rows from CSV and keep only those with bolt11.
    with open(path, "r", newline="", encoding="utf-8") as f:
        r = list(csv.DictReader(f))
    rows = [x for x in r if x.get("bolt11")]
    if not rows:
        raise RuntimeError("No rows with 'bolt11' found in invoices csv.")
    return rows

def pick_for_level(rows, level, n):
    # Pick the first N rows that match a concurrency level.
    cand = [r for r in rows if str(r.get("concurrency","")).strip() == str(level)]
    cand.sort(key=lambda x: int(x.get("index","0")) if str(x.get("index","0")).isdigit() else 0)
    if len(cand) < n:
        raise RuntimeError(f"Need {n} invoices for concurrency={level}, found {len(cand)}")
    return cand[:n]

def pay_one(container, network, bolt11, timeout_s):
    # Pay a single invoice using lncli payinvoice.
    start = utc_now_iso()
    t0 = time.time()

    # Build the lncli command.
    cmd = [
        "docker", "exec", container,
        "lncli", f"--network={network}",
        "payinvoice", "--force", "--json",
        "--pay_req", bolt11
    ]
    rc, out, err, _ = run_cmd(cmd, timeout=timeout_s)

    end = utc_now_iso()
    latency_ms = int(round((time.time() - t0) * 1000))

    # Parse the JSON output if possible.
    j = safe_json(out.strip()) if out else None
    success = (rc == 0) and (j is not None) and (j.get("payment_error", "") == "")

    # Base result row.
    row = {
        "start_ts": start,
        "end_ts": end,
        "latency_ms": latency_ms,
        "success": 1 if success else 0,
        "payment_hash": "",
        "preimage": "",
        "fee_msat": "",
        "value_msat": "",
        "payment_error": "",
        "lncli_returncode": rc,
    }

    if j:
        # Pull fields from lncli JSON.
        row["payment_hash"] = j.get("payment_hash","") or ""
        row["preimage"] = j.get("payment_preimage","") or ""
        row["fee_msat"] = str(j.get("fee_msat","")) if j.get("fee_msat") is not None else ""
        row["value_msat"] = str(j.get("value_msat","")) if j.get("value_msat") is not None else ""
        row["payment_error"] = (j.get("payment_error","") or "")[:5000]
    else:
        # Fall back to stderr/stdout snippet.
        msg = (err or out or "").strip()
        row["payment_error"] = msg[:5000]

    return row

def summarize(rows):
    # Compute basic success/latency stats for a batch.
    lat = [int(r["latency_ms"]) for r in rows]
    ok = sum(1 for r in rows if int(r["success"]) == 1)
    fail = len(rows) - ok
    return {
        "count": len(rows),
        "success_count": ok,
        "failure_count": fail,
        "success_rate": (ok / len(rows)) if rows else 0.0,
        "latency_ms_mean": mean(lat) if lat else 0.0,
        "latency_ms_median": median(lat) if lat else 0.0,
        "latency_ms_min": min(lat) if lat else 0,
        "latency_ms_max": max(lat) if lat else 0,
    }

def main():
    # Parse args and run payments for each concurrency level.
    ap = argparse.ArgumentParser()
    ap.add_argument("--container", default="lnd-testnet")
    ap.add_argument("--network", default="testnet")
    ap.add_argument("--invoices-csv", required=True)
    ap.add_argument("--levels", default="5,10,20,50,100")
    ap.add_argument("--timeout", type=int, default=120)
    ap.add_argument("--outdir", default=".")
    ap.add_argument("--run-id", default="")
    args = ap.parse_args()

    ensure_lncli(args.container, args.network)

    levels = [int(x.strip()) for x in args.levels.split(",") if x.strip()]
    run_id = args.run_id.strip() or f"payrun-{datetime.now().strftime('%Y%m%d-%H%M%S')}-{uuid.uuid4().hex[:8]}"

    # Load invoices and prepare output files.
    inv = load_invoices(args.invoices_csv)

    os.makedirs(args.outdir, exist_ok=True)
    raw_path = os.path.join(args.outdir, "payments_raw.csv")
    sum_path = os.path.join(args.outdir, "payments_summary.csv")

    raw_fields = ["runId","concurrency","requestId","index","bolt11",
                  "start_ts","end_ts","latency_ms","success",
                  "payment_hash","preimage","fee_msat","value_msat",
                  "payment_error","lncli_returncode"]
    sum_fields = ["runId","concurrency","count","success_count","failure_count","success_rate",
                  "latency_ms_mean","latency_ms_median","latency_ms_min","latency_ms_max"]

    with open(raw_path, "w", newline="", encoding="utf-8") as f:
        csv.DictWriter(f, fieldnames=raw_fields).writeheader()
    with open(sum_path, "w", newline="", encoding="utf-8") as f:
        csv.DictWriter(f, fieldnames=sum_fields).writeheader()

    for level in levels:
        # Pick the invoices for this level and pay them in parallel.
        batch = pick_for_level(inv, level, level)
        results = []

        with ThreadPoolExecutor(max_workers=level) as ex:
            futs = {}
            for row in batch:
                fut = ex.submit(pay_one, args.container, args.network, row["bolt11"], args.timeout)
                futs[fut] = row

            for fut in as_completed(futs):
                # Merge invoice metadata with payment result.
                invrow = futs[fut]
                r = fut.result()
                out = {
                    "runId": run_id,
                    "concurrency": level,
                    "requestId": invrow.get("requestId",""),
                    "index": invrow.get("index",""),
                    "bolt11": invrow.get("bolt11",""),
                    **r
                }
                results.append(out)
                with open(raw_path, "a", newline="", encoding="utf-8") as f:
                    csv.DictWriter(f, fieldnames=raw_fields).writerow(out)

        # Write summary stats for this level.
        s = summarize(results)
        srow = {"runId": run_id, "concurrency": level, **s}
        with open(sum_path, "a", newline="", encoding="utf-8") as f:
            csv.DictWriter(f, fieldnames=sum_fields).writerow(srow)

        print(f"[{level}] success {s['success_count']}/{s['count']} ({s['success_rate']:.1%}) "
              f"| median {s['latency_ms_median']:.0f} ms")

    # Final output locations.
    print("Outputs:", raw_path, sum_path, "RunId:", run_id)

if __name__ == "__main__":
    # Script entrypoint.
    main()
