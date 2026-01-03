package io.github.ladder.backend.orders.api

import io.github.ladder.backend.orders.dto.CartItemAddRequest
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest
import io.github.ladder.backend.orders.dto.CartResponse
import io.github.ladder.backend.orders.dto.CheckoutResponse
import io.github.ladder.backend.orders.service.CartService
import io.github.ladder.backend.orders.service.CheckoutService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// REST controller for managing shopping carts and checkout process
@RestController
@RequestMapping("/api/v1/carts")
class CartController(
    private val cartService: CartService,
    private val checkoutService: CheckoutService
) {

    // Create a new shopping cart
    @PostMapping
    fun createCart(): CartResponse = cartService.createCart()

    // Retrieve the contents of a shopping cart by its ID
    @GetMapping("/{cartId}")
    fun getCart(@PathVariable cartId: UUID): CartResponse = cartService.getCart(cartId)

    // Add an item to the shopping cart
    @PostMapping("/{cartId}/items")
    fun addItemToCart(
        @PathVariable cartId: UUID,
        @RequestBody @Valid request: CartItemAddRequest
    ): CartResponse = cartService.addItem(cartId, request)

    // Update the quantity of a specific item in the shopping cart
    @PutMapping("/{cartId}/items/{itemId}")
    fun updateCartItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody @Valid request: CartItemUpdateRequest
    ): CartResponse = cartService.updateItem(cartId, itemId, request)

    // Remove an item from the shopping cart
    @DeleteMapping("/{cartId}/items/{itemId}")
    fun removeCartItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID
    ): CartResponse = cartService.removeItem(cartId, itemId)

    // Initiate the checkout process for the shopping cart to create an invoice from BTCPay Server
    @PostMapping("/{cartId}/checkout")
    fun checkout(
        @PathVariable cartId: UUID
    ): CheckoutResponse = checkoutService.createInvoice(cartId)
}
