package io.github.ladder.backend.orders.service

import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.orders.domain.CartStatus
import io.github.ladder.backend.orders.persistence.CartRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

// Abandoned carts would otherwise hold their stock reservation forever
@Service
class CartExpirySweeper(
    private val cartRepository: CartRepository,
    private val listingRepository: ListingRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 300_000, initialDelay = 120_000)
    @Transactional
    fun releaseExpiredCarts() {
        val expired = cartRepository.findByStatusAndExpiresAtBefore(CartStatus.ACTIVE, Instant.now())
        if (expired.isEmpty()) return

        expired.forEach { cart ->
            cart.items.forEach { item ->
                val listing = listingRepository.findByIdForUpdate(item.listing.id!!).orElse(null)
                    ?: return@forEach
                listing.decreaseReserved(minOf(item.quantity, listing.reservedQuantity))
                listingRepository.save(listing)
            }
            cart.status = CartStatus.EXPIRED
            cartRepository.save(cart)
        }
        log.info("Released reservations of {} expired cart(s)", expired.size)
    }
}
