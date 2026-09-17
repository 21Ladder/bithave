package io.github.ladder.backend.orders.domain

// ACTIVE carts can be edited, CONVERTED ones belong to an order, EXPIRED ones released their stock
enum class CartStatus {
    ACTIVE,
    CONVERTED,
    EXPIRED
}
