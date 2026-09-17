package io.github.ladder.backend.orders.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

// Price and title are snapshots, so a paid order stays readable if the listing changes later
@Entity
@Table(name = "order_items")
class OrderItemEntity() {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity? = null

    @Column(name = "listing_id", nullable = false)
    var listingId: UUID = UUID(0, 0)

    @Column(name = "title_snapshot", nullable = false, length = 120)
    var titleSnapshot: String = ""

    @Column(name = "unit_price_cents", nullable = false)
    var unitPriceCents: Long = 0

    @Column(nullable = false)
    var quantity: Int = 0

    constructor(
        listingId: UUID,
        titleSnapshot: String,
        unitPriceCents: Long,
        quantity: Int
    ) : this() {
        this.listingId = listingId
        this.titleSnapshot = titleSnapshot
        this.unitPriceCents = unitPriceCents
        this.quantity = quantity
    }

    val subtotalCents: Long
        get() = unitPriceCents * quantity
}
