package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.BtcPayProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

// Invoice request for BTCPay Server
data class BtcPayInvoiceRequest(
    val amount: BigDecimal,
    val currency: String,
    val metadata: Map<String, Any?>? = null
)

// Invoice response from BTCPay Server, checkoutlink is there to redirect user to pay
data class BtcPayInvoiceResponse(
    val id: String,
    val status: String?,
    val checkoutLink: String?,
    val amount: BigDecimal?,
    val currency: String?
)

// Client to interact with BTCPay Server API, which identifies by the store ID
@Component
class BtcPayClient(
    private val btcPayProperties: BtcPayProperties,
    private val btcpayWebClient: WebClient
) {

    // Create a new invoice on BTCPay Server, properties are in application config
    fun createInvoice(amount: BigDecimal, currency: String, metadata: Map<String, Any?>): BtcPayInvoiceResponse {
        val request = BtcPayInvoiceRequest(amount = amount, currency = currency, metadata = metadata)

        return btcpayWebClient
            .post()
            .uri("/api/v1/stores/{storeId}/invoices", btcPayProperties.storeId)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(BtcPayInvoiceResponse::class.java)
            .block() ?: throw IllegalStateException("Failed to create BTCPay invoice")
    }
}
