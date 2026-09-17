package io.github.ladder.backend.orders.api

import io.github.ladder.backend.orders.service.BtcPaySignatureVerifier
import io.github.ladder.backend.orders.service.BtcPayWebhookService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// Receives BTCPay invoice events. Authenticated by HMAC signature, not by a session.
@RestController
@RequestMapping("/api/v1/payments/btcpay")
class BtcPayWebhookController(
    private val signatureVerifier: BtcPaySignatureVerifier,
    private val webhookService: BtcPayWebhookService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // Takes the raw body, the signature is computed over exactly these bytes
    @PostMapping("/webhook")
    fun receive(
        @RequestBody rawBody: String,
        @RequestHeader(name = "BTCPay-Sig", required = false) signature: String?
    ): ResponseEntity<Void> {
        if (!signatureVerifier.isValid(rawBody, signature)) {
            log.warn("Rejected BTCPay webhook with invalid signature")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        return try {
            webhookService.handle(rawBody)
            ResponseEntity.ok().build()
        } catch (ex: Exception) {
            // Answer 200 anyway, a retry storm would not fix a malformed payload
            log.error("Failed to process BTCPay webhook", ex)
            ResponseEntity.ok().build()
        }
    }
}
