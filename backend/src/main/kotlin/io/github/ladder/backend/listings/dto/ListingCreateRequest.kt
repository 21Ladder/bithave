package io.github.ladder.backend.listings.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class ListingCreateRequest(
    @field:NotBlank val title: String,
    @field:NotNull @field:PositiveOrZero val priceUsd: Long?,
    @field:NotNull @field:PositiveOrZero val quantity: Int?,
    val categoryPath: String? = null,
    val images: List<String>? = null,
    @field:NotNull val sellerId: UUID?
)
