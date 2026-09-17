package io.github.ladder.backend.orders.mapper

import io.github.ladder.backend.listings.persistence.ListingEntity
import io.github.ladder.backend.orders.dto.CartItemResponse
import io.github.ladder.backend.orders.dto.CartResponse
import io.github.ladder.backend.orders.persistence.CartEntity
import io.github.ladder.backend.orders.persistence.CartItemEntity
import org.springframework.stereotype.Component


// Mapper to convert CartEntity to CartResponse
@Component
class CartMapper {

    // Convert CartEntity to CartResponse
    fun toResponse(cart: CartEntity): CartResponse {
        val items = cart.items
            .map { toItemResponse(it) }

        val totalQuantity = items.sumOf { it.quantity }
        val totalPriceCents = items.sumOf { it.subtotalCents }

        return CartResponse(
            id = cart.id ?: throw IllegalStateException("Cart has no id"),
            items = items,
            totalQuantity = totalQuantity,
            totalPriceCents = totalPriceCents
        )
    }

    // Helper function to convert CartItemEntity to CartItemResponse is used by toResponse
    private fun toItemResponse(item: CartItemEntity): CartItemResponse {
        val listing: ListingEntity = item.listing
        val subtotal = item.unitPriceCents * item.quantity
        val thumbnail = listing.images.firstOrNull()

        return CartItemResponse(
            id = item.id ?: throw IllegalStateException("Cart item has no id"),
            listingId = listing.id ?: throw IllegalStateException("Listing has no id"),
            title = listing.title,
            thumbnailUrl = thumbnail,
            unitPriceCents = item.unitPriceCents,
            quantity = item.quantity,
            subtotalCents = subtotal
        )
    }
}
