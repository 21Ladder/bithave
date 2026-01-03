package io.github.ladder.backend.orders.service

import io.github.ladder.backend.listings.domain.ListingStatus
import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.orders.dto.CartItemAddRequest
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest
import io.github.ladder.backend.orders.dto.CartResponse
import io.github.ladder.backend.orders.mapper.CartMapper
import io.github.ladder.backend.orders.persistence.CartEntity
import io.github.ladder.backend.orders.persistence.CartItemEntity
import io.github.ladder.backend.orders.persistence.CartRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Objects
import java.util.UUID

// Implementation of CartService for managing shopping carts like adding, updating, and removing items
@Service
class CartServiceImpl(
    private val cartRepository: CartRepository,
    private val listingRepository: ListingRepository,
    private val cartMapper: CartMapper
) : CartService {

    // Create a new shopping cart
    @Transactional
    override fun createCart(): CartResponse {
        val cart = CartEntity()
        cartRepository.save(cart)
        return cartMapper.toResponse(cart)
    }

    // Retrieve the contents of a shopping cart by its ID
    @Transactional(readOnly = true)
    override fun getCart(cartId: UUID): CartResponse {
        val cart = loadCart(cartId)
        return cartMapper.toResponse(cart)
    }

    // Add an item to the shopping cart
    @Transactional
    override fun addItem(cartId: UUID, request: CartItemAddRequest): CartResponse {
        val cart = loadCart(cartId)

        val quantity = request.quantity ?: 1
        if (quantity < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1")
        }

        val listing = listingRepository.findById(request.listingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found") }

        if (listing.status != ListingStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Listing is not available")
        }

        val existing = cart.items
            .firstOrNull { item -> Objects.equals(item.listing.id, listing.id) }

        val increment = quantity
        val available = listing.availableQuantity
        if (available < increment) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Not enough quantity available")
        }

        if (existing != null) {
            existing.quantity = existing.quantity + quantity
            existing.unitPriceUsd = listing.priceUsd
        } else {
            val newItem = CartItemEntity(listing, quantity, listing.priceUsd)
            cart.addItem(newItem)
        }

        listing.increaseReserved(increment)

        cartRepository.save(cart)
        return cartMapper.toResponse(cart)
    }

    // Update the quantity of a specific item in the shopping cart
    @Transactional
    override fun updateItem(cartId: UUID, itemId: UUID, request: CartItemUpdateRequest): CartResponse {
        val cart = loadCart(cartId)

        if (request.quantity < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1")
        }

        val item = cart.items.firstOrNull { ci -> Objects.equals(ci.id, itemId) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart")

        val listing = item.listing
        val oldQuantity = item.quantity
        val newQuantity = request.quantity
        val delta = newQuantity - oldQuantity

        when {
            delta > 0 -> {
                val available = listing.availableQuantity
                if (available < delta) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Not enough quantity available")
                }
                listing.increaseReserved(delta)
            }
            delta < 0 -> listing.decreaseReserved(-delta)
        }

        item.quantity = newQuantity

        cartRepository.save(cart)
        return cartMapper.toResponse(cart)
    }

    // Remove an item from the shopping cart
    @Transactional
    override fun removeItem(cartId: UUID, itemId: UUID): CartResponse {
        val cart = loadCart(cartId)

        val item = cart.items.firstOrNull { ci -> Objects.equals(ci.id, itemId) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart")

        val listing = item.listing
        listing.decreaseReserved(item.quantity)

        cart.removeItem(item)
        cartRepository.save(cart)
        return cartMapper.toResponse(cart)
    }

    // Helper function to load cart by ID or throw 404
    private fun loadCart(cartId: UUID): CartEntity {
        return cartRepository.findWithItemsById(cartId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found") }
    }
}
