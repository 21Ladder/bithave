package io.github.ladder.backend.orders.dto

import java.util.UUID

// Response representing a single item in the shopping cart
data class CartItemResponse(
    val id: UUID,
    val listingId: UUID,
    val title: String,
    val thumbnailUrl: String?,
    val unitPriceCents: Long,
    val quantity: Int,
    val subtotalCents: Long
)
