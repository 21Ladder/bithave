package io.github.ladder.backend.listings.persistence

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

@Entity
@Table(name = "categories")
class CategoryEntity(
    @Column(name = "path", nullable = false, length = 120)
    var path: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CategoryEntity? = null
) {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null
}
