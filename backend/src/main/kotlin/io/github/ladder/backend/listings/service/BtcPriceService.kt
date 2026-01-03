package io.github.ladder.backend.listings.service

import io.github.ladder.backend.listings.dto.BtcPriceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

// Service to fetch the current Bitcoin price from CoinGecko API
@Service
class BtcPriceService(
    builder: WebClient.Builder,
    @Value("\${coingecko.apiKey}") private val apiKey: String
) {

    private val webClient: WebClient = builder.baseUrl("https://api.coingecko.com/api/v3").build()

    fun fetchCurrentPrice(): BtcPriceResponse {
        val url =
            "/simple/price?ids=bitcoin&vs_currencies=eur&include_last_updated_at=true&x_cg_demo_api_key=$apiKey"

        val response = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(BtcPriceResponse::class.java)
            .block()
            ?: throw IllegalStateException("Failed to fetch BTC price")

        // touch parsed values to fail fast if the payload is incomplete
        response.bitcoin.eur as BigDecimal
        response.bitcoin.last_updated_at
        return response
    }
}
