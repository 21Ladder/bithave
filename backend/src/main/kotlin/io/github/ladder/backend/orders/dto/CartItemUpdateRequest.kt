package io.github.ladder.backend.orders.dto

import jakarta.validation.constraints.Min

// Request to update the quantity of a cart item
data class CartItemUpdateRequest(
    @field:Min(1) val quantity: Int
)
