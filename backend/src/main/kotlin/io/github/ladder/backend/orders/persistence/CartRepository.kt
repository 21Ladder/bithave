package io.github.ladder.backend.orders.persistence

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface CartRepository : JpaRepository<CartEntity, UUID> {
    @EntityGraph(attributePaths = ["items", "items.listing"])
    fun findWithItemsById(id: UUID): Optional<CartEntity>
}
