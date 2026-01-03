package io.github.ladder.backend.listings.api

import io.github.ladder.backend.listings.persistence.BtcPriceRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

// REST controller to provide the current Bitcoin price in Euro from the database
@RestController
@RequestMapping("/api/v1")
class BtcPriceController(
    private val btcPriceRepository: BtcPriceRepository
) {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException): String? = ex.message

    @GetMapping("/btcPrice")
    fun getCurrentBtcPrice(): Float {
        val btcPrice = btcPriceRepository.findById(1L)
            .orElseThrow { RuntimeException("BTC price not available yet") }
            .priceEuro
            ?: throw RuntimeException("BTC price not available yet")

        return btcPrice.toFloat()
    }
}
