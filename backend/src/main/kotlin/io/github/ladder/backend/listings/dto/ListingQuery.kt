package io.github.ladder.backend.listings.dto

import java.util.UUID

data class ListingQuery(
    val page: Int,
    val size: Int,
    val sort: String,
    val order: String,
    val q: String?,
    val status: String?,
    val category: String?,
    val minPriceCents: Long?,
    val maxPriceCents: Long?,
    val sellerId: UUID?
)
