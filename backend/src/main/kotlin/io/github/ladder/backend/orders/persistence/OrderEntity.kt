package io.github.ladder.backend.orders.persistence

import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.users.persistence.UserEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity() {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null

    @Column(name = "order_number", nullable = false, length = 32, updatable = false)
    var orderNumber: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT
        private set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    var cart: CartEntity? = null

    // Null for guest checkout, set when the buyer was signed in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null

    @Column(nullable = false, length = 255)
    var email: String = ""

    @Embedded
    var shippingAddress: ShippingAddress = ShippingAddress()

    @Column(name = "total_cents", nullable = false)
    var totalCents: Long = 0

    @Column(nullable = false, length = 3)
    var currency: String = "EUR"

    @Column(name = "btcpay_invoice_id", length = 128)
    var btcpayInvoiceId: String? = null

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var items: MutableList<OrderItemEntity> = mutableListOf()

    @Version
    @Column(nullable = false)
    var version: Long = 0

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @Column(name = "paid_at")
    var paidAt: Instant? = null

    constructor(
        orderNumber: String,
        cart: CartEntity,
        email: String,
        shippingAddress: ShippingAddress,
        totalCents: Long,
        currency: String
    ) : this() {
        this.orderNumber = orderNumber
        this.cart = cart
        this.email = email
        this.shippingAddress = shippingAddress
        this.totalCents = totalCents
        this.currency = currency
    }

    fun addItem(item: OrderItemEntity) {
        items.add(item)
        item.order = this
    }

    // Only moves along the allowed path, so a replayed webhook cannot revive a closed order
    fun transitionTo(target: OrderStatus) {
        if (status == target) return
        require(status.canTransitionTo(target)) { "Cannot move order from $status to $target" }
        status = target
        if (target == OrderStatus.PAID) {
            paidAt = Instant.now()
        }
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
