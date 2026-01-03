package io.github.ladder.backend.listings.service

import io.github.ladder.backend.listings.dto.ListingCreateRequest
import io.github.ladder.backend.listings.dto.ListingQuery
import io.github.ladder.backend.listings.dto.ListingResponse
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.ListingUpdateRequest
import io.github.ladder.backend.listings.dto.PageResponse
import java.util.UUID

// Service interface for managing listings
interface ListingService {
    fun list(q: ListingQuery): PageResponse<ListingSummary>
    fun getById(id: UUID): ListingResponse
    fun create(req: ListingCreateRequest): UUID
    fun update(id: UUID, req: ListingUpdateRequest): ListingResponse
    fun archive(id: UUID)
}
