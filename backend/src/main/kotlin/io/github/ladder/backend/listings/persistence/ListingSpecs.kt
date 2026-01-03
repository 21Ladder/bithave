package io.github.ladder.backend.listings.persistence

import io.github.ladder.backend.listings.domain.ListingStatus
import org.springframework.data.jpa.domain.Specification
import java.util.Locale
import java.util.UUID

object ListingSpecs {

    fun titleContains(q: String?): Specification<ListingEntity>? {
        if (q.isNullOrBlank()) return null
        val searchTerm = "%${q.lowercase(Locale.ROOT)}%"
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("title")), searchTerm)
        }
    }

    fun statusEquals(status: ListingStatus?): Specification<ListingEntity>? {
        status ?: return null
        return Specification { root, _, cb ->
            cb.equal(root.get<ListingStatus>("status"), status)
        }
    }

    fun priceMin(min: Long?): Specification<ListingEntity>? {
        min ?: return null
        return Specification { root, _, cb ->
            cb.greaterThanOrEqualTo(root.get("priceUsd"), min)
        }
    }

    fun priceMax(max: Long?): Specification<ListingEntity>? {
        max ?: return null
        return Specification { root, _, cb ->
            cb.lessThanOrEqualTo(root.get("priceUsd"), max)
        }
    }

    fun sellerIdEquals(sellerId: UUID?): Specification<ListingEntity>? {
        sellerId ?: return null
        return Specification { root, _, cb ->
            cb.equal(root.get<UUID>("sellerId"), sellerId)
        }
    }

    fun categoryPathStartingWithIgnoreCase(categoryPath: String?): Specification<ListingEntity>? {
        if (categoryPath.isNullOrBlank()) return null
        val prefix = categoryPath.trim().lowercase(Locale.ROOT) + "%"
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.join<Any, Any>("category").get("path")), prefix)
        }
    }
}
