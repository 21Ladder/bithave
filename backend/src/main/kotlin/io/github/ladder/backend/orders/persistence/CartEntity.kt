package io.github.ladder.backend.orders.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "carts")
class CartEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null

    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("createdAt ASC")
    var items: MutableList<CartItemEntity> = mutableListOf()

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    fun addItem(item: CartItemEntity) {
        items.add(item)
        item.cart = this
    }

    fun removeItem(item: CartItemEntity) {
        items.remove(item)
        item.cart = null
    }

    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
