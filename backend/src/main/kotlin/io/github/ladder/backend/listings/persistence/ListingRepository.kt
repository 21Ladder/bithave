package io.github.ladder.backend.listings.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.Optional
import java.util.UUID

interface ListingRepository : JpaRepository<ListingEntity, UUID>, JpaSpecificationExecutor<ListingEntity> {

    // Row lock for the reserve and settle paths, two buyers must not pass the stock check at once
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from ListingEntity l where l.id = :id")
    fun findByIdForUpdate(id: UUID): Optional<ListingEntity>
}
