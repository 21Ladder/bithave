package io.github.ladder.backend.listings.persistence

import io.github.ladder.backend.listings.domain.ListingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ListingRepository : JpaRepository<ListingEntity, UUID>, JpaSpecificationExecutor<ListingEntity> {
    fun findByStatus(status: ListingStatus, pageable: Pageable): Page<ListingEntity>
    fun findByCategory_PathStartingWithIgnoreCase(categoryPath: String, pageable: Pageable): Page<ListingEntity>
    fun findByStatusAndCategory_PathStartingWithIgnoreCase(status: ListingStatus, categoryPath: String, pageable: Pageable): Page<ListingEntity>
}
