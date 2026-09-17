package io.github.ladder.backend.orders.dto

import java.time.Instant

// Order as shown on the confirmation and history pages
data class OrderResponse(
    val orderNumber: String,
    val status: String,
    val email: String,
    val items: List<OrderItemResponse>,
    val totalCents: Long,
    val currency: String,
    val checkoutLink: String?,
    val createdAt: Instant,
    val paidAt: Instant?
)
