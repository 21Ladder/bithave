package io.github.ladder.backend.listings.dto

import io.github.ladder.backend.listings.domain.ListingStatus
import java.time.Instant
import java.util.UUID

data class ListingSummary(
    val id: UUID,
    val title: String,
    val priceCents: Long,
    val categoryPath: String,
    val status: ListingStatus,
    val thumbnailUrl: String?,
    val createdAt: Instant,
    val availableQuantity: Int
)
