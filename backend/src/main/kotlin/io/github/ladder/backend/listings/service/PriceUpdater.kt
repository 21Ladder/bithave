package io.github.ladder.backend.listings.service

import io.github.ladder.backend.listings.persistence.BtcPrice
import io.github.ladder.backend.listings.persistence.BtcPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

// Service to periodically update the Bitcoin price in Euro
@Service
class PriceUpdater(
    private val btcPriceService: BtcPriceService,
    private val btcPriceRepository: BtcPriceRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 1_800_000)
    fun updatePrice() {
        try {
            val response = btcPriceService.fetchCurrentPrice()
            val entry = btcPriceRepository.findById(1L).orElse(BtcPrice())
            entry.id = 1L
            entry.priceEuro = response.bitcoin.eur
            entry.priceTimestamp = Instant.now()
            btcPriceRepository.save(entry)

            log.info("Saved BTC price: {}", response.bitcoin.eur)
        } catch (e: Exception) {
            log.warn("Error fetching BTC price", e)
        }
    }
}
