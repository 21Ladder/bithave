package io.github.ladder.backend.listings.mapper

import io.github.ladder.backend.listings.dto.ListingCreateRequest
import io.github.ladder.backend.listings.dto.ListingResponse
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.ListingUpdateRequest
import io.github.ladder.backend.listings.persistence.ListingEntity

interface ListingMapper {
    fun toSummary(entity: ListingEntity): ListingSummary
    fun requestToEntity(req: ListingCreateRequest): ListingEntity
    fun applyUpdate(req: ListingUpdateRequest, target: ListingEntity)
    fun toOneResponse(entity: ListingEntity): ListingResponse
}
