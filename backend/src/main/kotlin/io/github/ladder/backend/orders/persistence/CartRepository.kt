package io.github.ladder.backend.orders.persistence

import io.github.ladder.backend.orders.domain.CartStatus
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface CartRepository : JpaRepository<CartEntity, UUID> {

    // images are mapped in the response, fetch them here to avoid a query per item
    @EntityGraph(attributePaths = ["items", "items.listing", "items.listing.images"])
    fun findWithItemsById(id: UUID): Optional<CartEntity>

    // Sweeper input: active carts whose reservation window has passed
    @EntityGraph(attributePaths = ["items", "items.listing"])
    fun findByStatusAndExpiresAtBefore(status: CartStatus, cutoff: Instant): List<CartEntity>
}
