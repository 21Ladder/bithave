package io.github.ladder.backend.orders.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// Subset of the BTCPay webhook body that we actually act on
@JsonIgnoreProperties(ignoreUnknown = true)
data class BtcPayWebhookPayload(
    val deliveryId: String? = null,
    val webhookId: String? = null,
    val originalDeliveryId: String? = null,
    val type: String? = null,
    val invoiceId: String? = null,
    val storeId: String? = null
)
