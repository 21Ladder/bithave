package io.github.ladder.backend.listings.api

import io.github.ladder.backend.listings.dto.ListingCreateRequest
import io.github.ladder.backend.listings.dto.ListingQuery
import io.github.ladder.backend.listings.dto.ListingResponse
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.ListingUpdateRequest
import io.github.ladder.backend.listings.dto.PageResponse
import io.github.ladder.backend.listings.service.ListingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

// REST controller for managing listings
@RestController
@RequestMapping("/api/v1")
class ListingsController(
    private val listingService: ListingService
) {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException): String? = ex.message

    // List listings with optional filters and pagination
    @GetMapping("/listings")
    fun list(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int,
        @RequestParam(name = "sort", defaultValue = "createdAt") sort: String,
        @RequestParam(name = "order", defaultValue = "DESC") order: String,
        @RequestParam(name = "status", required = false) status: String?,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "category", required = false) category: String?,
        @RequestParam(name = "minPriceSats", required = false) minPriceSats: Long?,
        @RequestParam(name = "maxPriceSats", required = false) maxPriceSats: Long?,
        @RequestParam(name = "sellerId", required = false) sellerId: UUID?
    ): PageResponse<ListingSummary> {

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceAtLeast(1).coerceAtMost(100)

        val safeQ = ListingControllerUtils.normalizeString(q)
        val safeStatus = ListingControllerUtils.normalizeString(status)
        val safeCategory = ListingControllerUtils.normalizeString(category)
        val safeMin = ListingControllerUtils.nonNegativeOrNull(minPriceSats)
        val safeMax = ListingControllerUtils.nonNegativeOrNull(maxPriceSats)

        if (safeMin != null && safeMax != null && safeMin > safeMax) {
            throw IllegalArgumentException("minPriceSats must be <= maxPriceSats")
        }

        val query = ListingQuery(
            page = safePage,
            size = safeSize,
            sort = sort,
            order = order,
            q = safeQ,
            status = safeStatus,
            category = safeCategory,
            minPriceSats = safeMin,
            maxPriceSats = safeMax,
            sellerId = sellerId
        )
        return listingService.list(query)
    }

    // Retrieve a single listing by its ID
    @GetMapping("/listings/{id}")
    fun getOneListing(@PathVariable id: UUID): ListingResponse = listingService.getById(id)

    // Archive (soft delete) a listing by its ID
    @PostMapping("/listings")
    fun createOneListing(@RequestBody @Valid req: ListingCreateRequest): ResponseEntity<ListingResponse> {
        val id = listingService.create(req)
        val location = URI.create("/api/v1/listings/$id")
        return ResponseEntity.created(location).body(listingService.getById(id))
    }

    // Update an existing listing by its ID
    @PatchMapping("/listings/{id}")
    fun updateOneListing(@PathVariable id: UUID, @RequestBody @Valid req: ListingUpdateRequest): ListingResponse {
        return listingService.update(id, req)
    }
}
