package io.github.ladder.backend.listings.persistence

import io.github.ladder.backend.listings.domain.ListingStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderColumn
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "listings")
class ListingEntity() {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null

    @Column(nullable = false, length = 120)
    var title: String = ""

    @Column(name = "price_cents", nullable = false)
    var priceCents: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    lateinit var category: CategoryEntity

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ListingStatus = ListingStatus.ACTIVE

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "integer default 0")
    var quantity: Int = 0
        set(value) {
            require(value >= 0) { "Quantity cannot be negative" }
            require(value >= reservedQuantity) { "Quantity cannot be less than reserved quantity" }
            field = value
        }

    @Column(name = "reserved_quantity", nullable = false, columnDefinition = "integer default 0")
    var reservedQuantity: Int = 0
        set(value) {
            require(value >= 0) { "Reserved quantity cannot be negative" }
            require(value <= quantity) { "Reserved quantity cannot exceed quantity" }
            field = value
        }

    // guards against two carts reserving the same last unit
    @Version
    @Column(nullable = false)
    var version: Long = 0

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @Column(name = "seller_id", nullable = false)
    var sellerId: UUID = UUID.randomUUID()

    @ElementCollection
    @CollectionTable(
        name = "listing_images",
        joinColumns = [JoinColumn(name = "listing_id")]
    )
    @OrderColumn(name = "position")
    @Column(name = "url", nullable = false, columnDefinition = "text")
    var images: List<String> = emptyList()

    val categoryPath: String
        get() = category.path

    val availableQuantity: Int
        get() = (quantity - reservedQuantity).coerceAtLeast(0)

    constructor(
        title: String,
        priceCents: Long,
        quantity: Int,
        category: CategoryEntity,
        images: List<String>,
        sellerId: UUID
    ) : this() {
        this.title = title
        this.priceCents = priceCents
        this.quantity = quantity.coerceAtLeast(0)
        this.category = category
        this.images = images.toList()
        this.sellerId = sellerId
        this.status = ListingStatus.ACTIVE
        this.createdAt = Instant.now()
        this.reservedQuantity = 0
    }

    @PrePersist
    fun prePersist() {
        updatedAt = createdAt
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun increaseReserved(amount: Int) {
        require(amount >= 0) { "amount must be positive" }
        reservedQuantity += amount
    }

    fun decreaseReserved(amount: Int) {
        require(amount >= 0) { "amount must be positive" }
        require(amount <= reservedQuantity) { "Cannot release more than currently reserved" }
        reservedQuantity -= amount
    }
}
