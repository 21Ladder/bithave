package io.github.ladder.backend.orders.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.ladder.backend.config.BtcPayProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import java.math.BigDecimal

// Invoice request for BTCPay Server
data class BtcPayInvoiceRequest(
    val amount: BigDecimal,
    val currency: String,
    val metadata: Map<String, Any?>? = null,
    val checkout: BtcPayCheckoutOptions? = null
)

// Checkout options control where the buyer lands and how long the invoice stays payable
data class BtcPayCheckoutOptions(
    val redirectURL: String? = null,
    val expirationMinutes: Int? = null
)

// Invoice response from BTCPay Server, checkoutLink is there to redirect user to pay
@JsonIgnoreProperties(ignoreUnknown = true)
data class BtcPayInvoiceResponse(
    val id: String,
    val status: String?,
    val checkoutLink: String?,
    val amount: BigDecimal?,
    val currency: String?
)

// Raised when BTCPay is unreachable or rejects a call
class BtcPayException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// Client to interact with BTCPay Server API, which identifies by the store ID
@Component
class BtcPayClient(
    private val btcPayProperties: BtcPayProperties,
    private val btcpayWebClient: WebClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // Create a new invoice on BTCPay Server, properties are in application config
    fun createInvoice(
        amount: BigDecimal,
        currency: String,
        metadata: Map<String, Any?>,
        redirectUrl: String? = null,
        expirationMinutes: Int? = null
    ): BtcPayInvoiceResponse {
        val request = BtcPayInvoiceRequest(
            amount = amount,
            currency = currency,
            metadata = metadata,
            checkout = BtcPayCheckoutOptions(redirectURL = redirectUrl, expirationMinutes = expirationMinutes)
        )

        return try {
            btcpayWebClient
                .post()
                .uri("/api/v1/stores/{storeId}/invoices", btcPayProperties.storeId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BtcPayInvoiceResponse::class.java)
                .block() ?: throw BtcPayException("BTCPay returned an empty invoice response")
        } catch (ex: WebClientException) {
            log.error("Creating BTCPay invoice failed", ex)
            throw BtcPayException("Could not create BTCPay invoice", ex)
        }
    }

    // Used to verify a webhook against BTCPay itself instead of trusting the payload
    fun getInvoice(invoiceId: String): BtcPayInvoiceResponse {
        return try {
            btcpayWebClient
                .get()
                .uri("/api/v1/stores/{storeId}/invoices/{invoiceId}", btcPayProperties.storeId, invoiceId)
                .retrieve()
                .bodyToMono(BtcPayInvoiceResponse::class.java)
                .block() ?: throw BtcPayException("BTCPay returned an empty invoice response")
        } catch (ex: WebClientException) {
            log.error("Fetching BTCPay invoice {} failed", invoiceId, ex)
            throw BtcPayException("Could not fetch BTCPay invoice", ex)
        }
    }
}
