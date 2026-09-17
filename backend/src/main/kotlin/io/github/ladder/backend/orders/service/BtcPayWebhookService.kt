package io.github.ladder.backend.orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.dto.BtcPayWebhookPayload
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// Applies a verified BTCPay webhook exactly once
@Service
class BtcPayWebhookService(
    private val objectMapper: ObjectMapper,
    private val settlementService: PaymentSettlementService,
    private val deduplicator: WebhookDeduplicator
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(rawBody: String) {
        val payload = objectMapper.readValue(rawBody, BtcPayWebhookPayload::class.java)
        val type = payload.type ?: return
        val invoiceId = payload.invoiceId

        if (invoiceId.isNullOrBlank()) {
            log.debug("Ignoring BTCPay webhook {} without invoiceId", type)
            return
        }

        // BTCPay retries deliveries, the marker turns a replay into a no-op
        val deliveryId = payload.deliveryId ?: "$type:$invoiceId"
        if (!deduplicator.claim(deliveryId, type)) {
            log.debug("Skipping already processed BTCPay delivery {}", deliveryId)
            return
        }

        when (type) {
            "InvoiceSettled" -> settlementService.settleFromInvoice(invoiceId)
            "InvoiceExpired" -> settlementService.releaseFromInvoice(invoiceId, OrderStatus.EXPIRED)
            "InvoiceInvalid" -> settlementService.releaseFromInvoice(invoiceId, OrderStatus.CANCELLED)
            else -> log.debug("Ignoring BTCPay event type {}", type)
        }
    }
}
