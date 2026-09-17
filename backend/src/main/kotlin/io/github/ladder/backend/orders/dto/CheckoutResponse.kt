package io.github.ladder.backend.orders.dto

// Returned after checkout, the client redirects to checkoutLink and later polls orderNumber
data class CheckoutResponse(
    val orderNumber: String,
    val status: String,
    val checkoutLink: String?,
    val totalCents: Long,
    val currency: String
)
