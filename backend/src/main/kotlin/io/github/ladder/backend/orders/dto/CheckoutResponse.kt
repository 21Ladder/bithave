package io.github.ladder.backend.orders.dto

// Response returned after creating a checkout invoice
data class CheckoutResponse(
    val invoiceId: String,
    val status: String?,
    val checkoutLink: String?,
    val amount: Double?,
    val currency: String?
)
