package io.github.ladder.backend.listings.dto;
import java.math.BigDecimal;
import java.util.Map;

public class BtcPriceResponse {
    public Bitcoin bitcoin;

    public static class Bitcoin {
        public BigDecimal eur;
        public long last_updated_at;
    }
}
