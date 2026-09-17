package io.github.ladder.backend.listings.dto

import java.math.BigDecimal
import java.time.Instant

// Current BTC price in Euro as served to clients
data class BtcPriceView(
    val priceEur: BigDecimal,
    val updatedAt: Instant?
)
