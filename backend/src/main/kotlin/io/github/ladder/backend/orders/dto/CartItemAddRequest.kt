package io.github.ladder.backend.orders.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

// Request to add an item to the shopping cart
data class CartItemAddRequest(
    @field:NotNull val listingId: UUID,
    @field:Min(1) val quantity: Int? = null
)
