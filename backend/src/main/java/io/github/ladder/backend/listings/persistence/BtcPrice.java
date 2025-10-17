package io.github.ladder.backend.listings.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "btc_price")
public class BtcPrice {
    @Id
    private Long id;

    private BigDecimal priceEuro;
    private Instant priceTimestamp;
    private Instant fetchedAt;

    public BigDecimal getPriceEuro() {
        return this.priceEuro;
    }
    public void setPriceEuro(BigDecimal priceEuro) {
        this.priceEuro = priceEuro;
    };

    public void setPriceTimestamp(Instant priceTimestamp) {
        this.priceTimestamp = priceTimestamp;
    }

    public void setId(long l) {
        this.id = l;
    }
}
