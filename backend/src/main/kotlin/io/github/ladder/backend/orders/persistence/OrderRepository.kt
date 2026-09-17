package io.github.ladder.backend.orders.persistence

import io.github.ladder.backend.orders.domain.OrderStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface OrderRepository : JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = ["items"])
    fun findWithItemsByOrderNumber(orderNumber: String): Optional<OrderEntity>

    @EntityGraph(attributePaths = ["items"])
    fun findWithItemsByBtcpayInvoiceId(btcpayInvoiceId: String): Optional<OrderEntity>

    // Locked because settlement adjusts stock based on it
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    fun findByIdForUpdate(id: UUID): Optional<OrderEntity>

    fun findFirstByCart_IdAndStatus(cartId: UUID, status: OrderStatus): Optional<OrderEntity>

    fun existsByOrderNumber(orderNumber: String): Boolean

    @EntityGraph(attributePaths = ["items"])
    fun findByUser_IdOrderByCreatedAtDesc(userId: UUID): List<OrderEntity>

    // Fallback poller input: pending orders that should have settled by now
    fun findByStatusAndCreatedAtBefore(status: OrderStatus, cutoff: Instant): List<OrderEntity>
}
