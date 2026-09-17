package io.github.ladder.backend.listings.mapper

import io.github.ladder.backend.listings.dto.ListingCreateRequest
import io.github.ladder.backend.listings.dto.ListingResponse
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.ListingUpdateRequest
import io.github.ladder.backend.listings.persistence.CategoryRepository
import io.github.ladder.backend.listings.persistence.ListingEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListingMapperImpl(
    private val categoryRepository: CategoryRepository
) : ListingMapper {

    override fun toSummary(entity: ListingEntity): ListingSummary {
        val thumbnail = entity.images.firstOrNull()
        return ListingSummary(
            id = entity.id ?: throw IllegalStateException("Listing has no id"),
            title = entity.title,
            priceCents = entity.priceCents,
            categoryPath = entity.categoryPath,
            status = entity.status,
            thumbnailUrl = thumbnail,
            createdAt = entity.createdAt,
            availableQuantity = entity.availableQuantity
        )
    }

    override fun requestToEntity(req: ListingCreateRequest, sellerId: UUID): ListingEntity {
        val images = req.images?.toList().orEmpty()

        val quantity = req.quantity
            ?: throw IllegalArgumentException("quantity required")
        require(quantity >= 0) { "quantity cannot be negative" }

        val rawCategory = req.categoryPath
            ?.trim()
            ?.lowercase()
            ?: throw IllegalArgumentException("categoryPath required")
        if (rawCategory.isEmpty()) {
            throw IllegalArgumentException("categoryPath required")
        }

        val category = categoryRepository.findByPath(rawCategory)
            .orElseThrow { IllegalArgumentException("Category path not found: ${req.categoryPath}") }

        return ListingEntity(
            title = req.title,
            priceCents = req.priceCents
                ?: throw IllegalArgumentException("priceCents required"),
            quantity = quantity,
            category = category,
            images = images,
            sellerId = sellerId
        )
    }

    override fun applyUpdate(req: ListingUpdateRequest, target: ListingEntity) {
        req.title?.let {
            val trimmed = it.trim()
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("Title cannot be empty")
            }
            target.title = trimmed
        }

        req.priceCents?.let {
            require(it >= 0) { "priceCents cannot be negative" }
            target.priceCents = it
        }

        req.status?.let { target.status = it }

        req.quantity?.let { target.quantity = it }

        req.images?.let { target.images = it.toList() }

        req.categoryPath?.let { path ->
            val cp = path.trim().lowercase()
            val category = categoryRepository.findByPath(cp)
                .orElseThrow { IllegalArgumentException("Category path not found: ${req.categoryPath}") }
            target.category = category
        }
    }

    override fun toOneResponse(entity: ListingEntity): ListingResponse {
        val safeImages = entity.images.ifEmpty { emptyList() }

        return ListingResponse(
            id = entity.id ?: throw IllegalStateException("Listing has no id"),
            title = entity.title,
            priceCents = entity.priceCents,
            categoryPath = entity.categoryPath,
            status = entity.status,
            images = safeImages,
            sellerId = entity.sellerId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            quantity = entity.quantity,
            reservedQuantity = entity.reservedQuantity,
            availableQuantity = entity.availableQuantity
        )
    }
}
