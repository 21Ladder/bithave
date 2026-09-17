package io.github.ladder.backend.listings.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

// sellerId is set from the signed in operator, never from the client
data class ListingCreateRequest(
    @field:NotBlank val title: String,
    @field:NotNull @field:PositiveOrZero val priceCents: Long?,
    @field:NotNull @field:PositiveOrZero val quantity: Int?,
    val categoryPath: String? = null,
    val images: List<String>? = null
)
