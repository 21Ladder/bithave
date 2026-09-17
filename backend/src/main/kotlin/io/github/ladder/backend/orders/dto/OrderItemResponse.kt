package io.github.ladder.backend.orders.dto

import java.util.UUID

// One line of a placed order
data class OrderItemResponse(
    val listingId: UUID,
    val title: String,
    val unitPriceCents: Long,
    val quantity: Int,
    val subtotalCents: Long
)
