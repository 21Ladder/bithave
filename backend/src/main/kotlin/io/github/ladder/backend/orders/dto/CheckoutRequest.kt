package io.github.ladder.backend.orders.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

// Everything needed to turn a cart into an order
data class CheckoutRequest(
    @field:NotNull val cartId: UUID?,
    @field:NotBlank @field:Email val email: String?,
    @field:NotNull @field:Valid val shippingAddress: ShippingAddressRequest?
)
