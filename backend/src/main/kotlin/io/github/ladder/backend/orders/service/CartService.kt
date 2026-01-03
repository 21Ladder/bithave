package io.github.ladder.backend.orders.service

import io.github.ladder.backend.orders.dto.CartItemAddRequest
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest
import io.github.ladder.backend.orders.dto.CartResponse
import java.util.UUID

// Service interface for managing shopping carts like adding, updating, and removing items
interface CartService {
    fun createCart(): CartResponse
    fun getCart(cartId: UUID): CartResponse
    fun addItem(cartId: UUID, request: CartItemAddRequest): CartResponse
    fun updateItem(cartId: UUID, itemId: UUID, request: CartItemUpdateRequest): CartResponse
    fun removeItem(cartId: UUID, itemId: UUID): CartResponse
}
