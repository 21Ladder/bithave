package io.github.ladder.backend.listings.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "btc_price")
class BtcPrice(
    @Id
    var id: Long? = null,
    var priceEuro: BigDecimal? = null,
    var priceTimestamp: Instant? = null,
    var fetchedAt: Instant? = null
)
