package io.github.ladder.backend.listings.dto

import io.github.ladder.backend.listings.domain.ListingStatus
import jakarta.validation.constraints.PositiveOrZero

data class ListingUpdateRequest(
    val title: String? = null,
    @field:PositiveOrZero val priceCents: Long? = null,
    @field:PositiveOrZero val quantity: Int? = null,
    val categoryPath: String? = null,
    val status: ListingStatus? = null,
    val images: List<String>? = null
)
