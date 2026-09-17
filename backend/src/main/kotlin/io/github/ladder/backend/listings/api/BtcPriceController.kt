package io.github.ladder.backend.listings.api

import io.github.ladder.backend.listings.dto.BtcPriceView
import io.github.ladder.backend.listings.persistence.BtcPriceRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

// REST controller to provide the current Bitcoin price in Euro from the database
@RestController
@RequestMapping("/api/v1")
class BtcPriceController(
    private val btcPriceRepository: BtcPriceRepository
) {

    @GetMapping("/btcPrice")
    fun getCurrentBtcPrice(): BtcPriceView {
        val entry = btcPriceRepository.findById(1L).orElse(null)
        val price = entry?.priceEuro
            ?: throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "BTC price not available yet")

        return BtcPriceView(priceEur = price, updatedAt = entry.priceTimestamp)
    }
}
