package io.github.ladder.backend.listings.dto

import java.math.BigDecimal

data class BtcPriceResponse(
    val bitcoin: Bitcoin
) {
    data class Bitcoin(
        val eur: BigDecimal,
        val last_updated_at: Long
    )
}
