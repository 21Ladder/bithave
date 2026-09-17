package io.github.ladder.backend.orders.dto

import java.util.UUID

// Response representing the contents of a shopping cart
data class CartResponse(
    val id: UUID,
    val items: List<CartItemResponse>,
    val totalQuantity: Int,
    val totalPriceCents: Long
)
