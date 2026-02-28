#!/usr/bin/env python3
# Simple load test for BTCPay invoice creation and payment-method fetch.
import argparse
import asyncio
import csv
import json
import os
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional, Tuple

import aiohttp


def utc_iso() -> str:
    # Current UTC time as ISO string.
    return datetime.now(timezone.utc).isoformat()


def ms(ns_start: int, ns_end: int) -> float:
    # Convert nanosecond duration to milliseconds.
    return (ns_end - ns_start) / 1_000_000.0


@dataclass
class Row:
    # One row of raw results for a single invoice request.
    run_id: str
    concurrency: int
    request_index: int
    request_id: str

    create_started_utc: str
    create_latency_ms: float
    create_http_status: Optional[int]
    create_success: bool
    create_error: Optional[str]

    invoice_id: Optional[str]
    invoice_status: Optional[str]
    checkout_link: Optional[str]

    pm_started_utc: Optional[str]
    pm_latency_ms: Optional[float]
    pm_http_status: Optional[int]
    pm_success: Optional[bool]
    pm_error: Optional[str]

    bolt11: Optional[str]
    payment_hash: Optional[str]
    node_invoice_id: Optional[str]
    node_info: Optional[str]


async def http_post_json(
    session: aiohttp.ClientSession,
    url: str,
    headers: Dict[str, str],
    payload: Dict[str, Any],
    timeout_s: float,
) -> Tuple[Optional[int], Optional[Dict[str, Any]], Optional[str], float]:
    # POST JSON and return status, parsed JSON, error text, and latency.
    t0 = time.perf_counter_ns()
    try:
        async with session.post(url, headers=headers, json=payload, timeout=timeout_s) as resp:
            status = resp.status
            text = await resp.text()
            t1 = time.perf_counter_ns()
            if 200 <= status < 300:
                try:
                    return status, json.loads(text), None, ms(t0, t1)
                except Exception as e:
                    return status, None, f"JSON parse error: {e}; body={text[:300]}", ms(t0, t1)
            return status, None, f"HTTP {status}: {text[:300]}", ms(t0, t1)
    except Exception as e:
        t1 = time.perf_counter_ns()
        return None, None, f"Request error: {repr(e)}", ms(t0, t1)


async def http_get_json(
    session: aiohttp.ClientSession,
    url: str,
    headers: Dict[str, str],
    timeout_s: float,
) -> Tuple[Optional[int], Optional[Any], Optional[str], float]:
    # GET JSON and return status, parsed JSON, error text, and latency.
    t0 = time.perf_counter_ns()
    try:
        async with session.get(url, headers=headers, timeout=timeout_s) as resp:
            status = resp.status
            text = await resp.text()
            t1 = time.perf_counter_ns()
            if 200 <= status < 300:
                try:
                    return status, json.loads(text), None, ms(t0, t1)
                except Exception as e:
                    return status, None, f"JSON parse error: {e}; body={text[:300]}", ms(t0, t1)
            return status, None, f"HTTP {status}: {text[:300]}", ms(t0, t1)
    except Exception as e:
        t1 = time.perf_counter_ns()
        return None, None, f"Request error: {repr(e)}", ms(t0, t1)


def extract_ln_payment_method(payment_methods: Any) -> Tuple[Optional[str], Optional[str], Optional[str], Optional[str]]:
    """
    Matches your observed structure:
    - list of objects
    - lightning invoice for BTC-LN is in 'destination'
    - additionalData.paymentHash, additionalData.invoiceId, additionalData.nodeInfo
    """
    # Pull the BTC-LN fields we care about if present.
    if not isinstance(payment_methods, list):
        return None, None, None, None
    for pm in payment_methods:
        if isinstance(pm, dict) and pm.get("paymentMethodId") == "BTC-LN":
            bolt11 = pm.get("destination")
            add = pm.get("additionalData") if isinstance(pm.get("additionalData"), dict) else {}
            payment_hash = add.get("paymentHash")
            node_invoice_id = add.get("invoiceId")
            node_info = add.get("nodeInfo")
            return bolt11, payment_hash, node_invoice_id, node_info
    return None, None, None, None


async def run_concurrency_level(
    run_id: str,
    concurrency: int,
    base_url: str,
    store_id: str,
    api_key: str,
    timeout_s: float,
) -> List[Row]:
    # Run a batch of concurrent invoice creations and return raw rows.
    connector = aiohttp.TCPConnector(limit=0)
    timeout = aiohttp.ClientTimeout(total=None)
    headers = {
        "Authorization": f"token {api_key}",
        "Accept": "application/json",
        "Content-Type": "application/json",
    }

    # Endpoint for creating invoices.
    create_url = f"{base_url.rstrip('/')}/api/v1/stores/{store_id}/invoices"

    rows: List[Row] = []
    # Limit in-flight tasks to the requested concurrency.
    sem = asyncio.Semaphore(concurrency)

    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:

        async def one(i: int) -> None:
            # One invoice creation + optional payment-method fetch.
            async with sem:
                request_id = f"{run_id}-c{concurrency}-i{i}"
                create_started = utc_iso()

                # Minimal invoice payload with tracking metadata.
                payload = {
                    "amount": 1,
                    "currency": "SATS",
                    "metadata": {
                        "study": "invoice-loadtest",
                        "runId": run_id,
                        "requestId": request_id,
                        "concurrency": concurrency,
                        "index": i,
                    },
                }

                st, body, err, lat = await http_post_json(session, create_url, headers, payload, timeout_s)
                success = (err is None) and isinstance(body, dict)

                # Extract basic invoice fields when creation worked.
                invoice_id = body.get("id") if success else None
                invoice_status = body.get("status") if success else None
                checkout_link = body.get("checkoutLink") if success else None

                # Payment-methods fetch.
                pm_started = None
                pm_lat = None
                pm_st = None
                pm_ok = None
                pm_err = None

                bolt11 = None
                payment_hash = None
                node_invoice_id = None
                node_info = None

                if success and invoice_id:
                    # Call the payment-methods endpoint for LN details.
                    pm_started = utc_iso()
                    pm_url = f"{base_url.rstrip('/')}/api/v1/stores/{store_id}/invoices/{invoice_id}/payment-methods"
                    pm_st, pm_body, pm_err, pm_lat = await http_get_json(session, pm_url, headers, timeout_s)
                    pm_ok = (pm_err is None) and isinstance(pm_body, list)
                    if pm_ok:
                        bolt11, payment_hash, node_invoice_id, node_info = extract_ln_payment_method(pm_body)

                # Save one row of results.
                rows.append(Row(
                    run_id=run_id,
                    concurrency=concurrency,
                    request_index=i,
                    request_id=request_id,
                    create_started_utc=create_started,
                    create_latency_ms=lat,
                    create_http_status=st,
                    create_success=success,
                    create_error=err,
                    invoice_id=invoice_id,
                    invoice_status=invoice_status,
                    checkout_link=checkout_link,
                    pm_started_utc=pm_started,
                    pm_latency_ms=pm_lat,
                    pm_http_status=pm_st,
                    pm_success=pm_ok,
                    pm_error=pm_err,
                    bolt11=bolt11,
                    payment_hash=payment_hash,
                    node_invoice_id=node_invoice_id,
                    node_info=node_info,
                ))

        # Launch all tasks for this concurrency level.
        await asyncio.gather(*[one(i) for i in range(concurrency)])

    return rows


def write_csv(path: str, rows: List[Row]) -> None:
    # Write the raw per-request results to CSV.
    fields = [
        "run_id", "concurrency", "request_index", "request_id",
        "create_started_utc", "create_latency_ms", "create_http_status", "create_success", "create_error",
        "invoice_id", "invoice_status", "checkout_link",
        "pm_started_utc", "pm_latency_ms", "pm_http_status", "pm_success", "pm_error",
        "bolt11", "payment_hash", "node_invoice_id", "node_info",
    ]
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        for r in rows:
            # Keep CSV values flat and readable.
            w.writerow({
                "run_id": r.run_id,
                "concurrency": r.concurrency,
                "request_index": r.request_index,
                "request_id": r.request_id,
                "create_started_utc": r.create_started_utc,
                "create_latency_ms": f"{r.create_latency_ms:.3f}",
                "create_http_status": r.create_http_status,
                "create_success": r.create_success,
                "create_error": r.create_error,
                "invoice_id": r.invoice_id,
                "invoice_status": r.invoice_status,
                "checkout_link": r.checkout_link,
                "pm_started_utc": r.pm_started_utc,
                "pm_latency_ms": f"{r.pm_latency_ms:.3f}" if r.pm_latency_ms is not None else "",
                "pm_http_status": r.pm_http_status,
                "pm_success": r.pm_success,
                "pm_error": r.pm_error,
                "bolt11": r.bolt11,
                "payment_hash": r.payment_hash,
                "node_invoice_id": r.node_invoice_id,
                "node_info": r.node_info,
            })


def write_summary(path: str, rows: List[Row]) -> None:
    # Write summary stats grouped by concurrency.
    by: Dict[int, List[Row]] = {}
    for r in rows:
        by.setdefault(r.concurrency, []).append(r)

    def pct(vals: List[float], p: float) -> float:
        # Simple percentile helper.
        s = sorted(vals)
        k = (len(s)-1) * p/100.0
        f = int(k)
        c = min(f+1, len(s)-1)
        if f == c:
            return s[f]
        return s[f] + (s[c]-s[f])*(k-f)

    fields = [
        "run_id", "concurrency",
        "count",
        "create_success_rate",
        "pm_success_rate",
        "create_avg_ms", "create_p50_ms", "create_p95_ms", "create_p99_ms",
        "pm_avg_ms", "pm_p50_ms", "pm_p95_ms", "pm_p99_ms",
        "bolt11_present_rate",
    ]

    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()

        for conc, rs in sorted(by.items()):
            # Compute basic rates and latency stats.
            create_lat = [r.create_latency_ms for r in rs]
            pm_lat = [r.pm_latency_ms for r in rs if r.pm_latency_ms is not None]

            create_ok = sum(1 for r in rs if r.create_success)
            pm_ok = sum(1 for r in rs if r.pm_success)
            bolt_ok = sum(1 for r in rs if r.bolt11)

            w.writerow({
                "run_id": rs[0].run_id,
                "concurrency": conc,
                "count": len(rs),
                "create_success_rate": f"{create_ok/len(rs):.4f}",
                "pm_success_rate": f"{pm_ok/len(rs):.4f}",
                "create_avg_ms": f"{sum(create_lat)/len(create_lat):.3f}",
                "create_p50_ms": f"{pct(create_lat,50):.3f}",
                "create_p95_ms": f"{pct(create_lat,95):.3f}",
                "create_p99_ms": f"{pct(create_lat,99):.3f}",
                "pm_avg_ms": f"{(sum(pm_lat)/len(pm_lat)):.3f}" if pm_lat else "",
                "pm_p50_ms": f"{pct(pm_lat,50):.3f}" if pm_lat else "",
                "pm_p95_ms": f"{pct(pm_lat,95):.3f}" if pm_lat else "",
                "pm_p99_ms": f"{pct(pm_lat,99):.3f}" if pm_lat else "",
                "bolt11_present_rate": f"{bolt_ok/len(rs):.4f}",
            })


async def main_async() -> None:
    # Parse args and run the requested load sweep.
    ap = argparse.ArgumentParser()
    ap.add_argument("--base-url", required=True)
    ap.add_argument("--store-id", required=True)
    ap.add_argument("--api-key", required=True)
    ap.add_argument("--levels", default="5,10,20,50,100")
    ap.add_argument("--runs", type=int, default=1, help="repeat the sweep N times")
    ap.add_argument("--timeout", type=float, default=20.0)
    ap.add_argument("--outdir", default="results")
    args = ap.parse_args()

    levels = [int(x.strip()) for x in args.levels.split(",") if x.strip()]
    os.makedirs(args.outdir, exist_ok=True)

    all_rows: List[Row] = []

    for run in range(1, args.runs + 1):
        # Each run has a unique ID and sweeps all levels.
        run_id = f"run{run}-{datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ')}"
        print(f"\n=== {run_id} levels={levels} ===")
        for conc in levels:
            print(f" -> concurrency {conc}")
            rs = await run_concurrency_level(run_id, conc, args.base_url, args.store_id, args.api_key, args.timeout)
            ok = sum(1 for r in rs if r.create_success)
            print(f"    create success {ok}/{len(rs)} | avg create latency {sum(r.create_latency_ms for r in rs)/len(rs):.1f} ms")
            all_rows.extend(rs)

    # Write final CSV outputs.
    raw_path = os.path.join(args.outdir, "invoices_raw.csv")
    summary_path = os.path.join(args.outdir, "invoices_summary.csv")
    write_csv(raw_path, all_rows)
    write_summary(summary_path, all_rows)

    print("\nWrote:")
    print(f"  {raw_path}")
    print(f"  {summary_path}")


def main() -> None:
    # Entrypoint wrapper for asyncio.
    asyncio.run(main_async())


if __name__ == "__main__":
    main()
