package io.github.ladder.backend.listings.dto

import io.github.ladder.backend.listings.domain.ListingStatus
import java.time.Instant
import java.util.UUID

data class ListingResponse(
    val id: UUID,
    val title: String,
    val priceUsd: Long,
    val categoryPath: String,
    val status: ListingStatus,
    val images: List<String>,
    val sellerId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val quantity: Int,
    val reservedQuantity: Int,
    val availableQuantity: Int
)
