package io.github.ladder.backend.orders.domain

// Lifecycle of an order, driven by BTCPay invoice events
enum class OrderStatus {
    PENDING_PAYMENT,
    PAID,
    FULFILLED,
    EXPIRED,
    CANCELLED;

    // Terminal states never change again
    val isTerminal: Boolean
        get() = this == FULFILLED || this == EXPIRED || this == CANCELLED

    fun canTransitionTo(target: OrderStatus): Boolean = when (this) {
        PENDING_PAYMENT -> target == PAID || target == EXPIRED || target == CANCELLED
        PAID -> target == FULFILLED || target == CANCELLED
        FULFILLED, EXPIRED, CANCELLED -> false
    }
}
