package io.github.ladder.backend.listings.service

import io.github.ladder.backend.listings.domain.ListingStatus
import io.github.ladder.backend.listings.dto.ListingCreateRequest
import io.github.ladder.backend.listings.dto.ListingQuery
import io.github.ladder.backend.listings.dto.ListingResponse
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.ListingUpdateRequest
import io.github.ladder.backend.listings.dto.PageResponse
import io.github.ladder.backend.listings.mapper.ListingMapper
import io.github.ladder.backend.listings.persistence.ListingEntity
import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.listings.persistence.ListingSpecs
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Locale
import java.util.UUID

@Service
class ListingServiceImpl(
    private val repo: ListingRepository,
    private val mapper: ListingMapper
) : ListingService {

    @Transactional(readOnly = true)
    override fun list(q: ListingQuery): PageResponse<ListingSummary> {
        val page = q.page.coerceAtLeast(0)
        val size = if (q.size > 0) q.size.coerceAtMost(100) else 20

        val requestedSort = q.sort.trim()
        val sortField = when (requestedSort) {
            "priceusd", "priceUsd", "PRICEUSD" -> "priceUsd"
            "createdAt", "createdat", "CREATEDAT" -> "createdAt"
            else -> "createdAt"
        }

        val order = q.order.trim().uppercase(Locale.ROOT)
        val dir = if (order == "ASC") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(dir, sortField))

        val statusStr = q.status?.trim().orEmpty()
        val parsedStatus = if (statusStr.isNotEmpty()) {
            try {
                ListingStatus.valueOf(statusStr.uppercase(Locale.ROOT))
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported status: $statusStr (use ACTIVE|SOLD|ARCHIVED)")
            }
        } else null

        var spec: Specification<ListingEntity>? = null
        spec = and(spec, ListingSpecs.titleContains(q.q))
        spec = and(spec, ListingSpecs.statusEquals(parsedStatus))
        spec = and(spec, ListingSpecs.priceMin(q.minPriceSats))
        spec = and(spec, ListingSpecs.priceMax(q.maxPriceSats))
        spec = and(spec, ListingSpecs.sellerIdEquals(q.sellerId))
        spec = and(spec, ListingSpecs.categoryPathStartingWithIgnoreCase(q.category))

        val pageData = repo.findAll(spec, pageable)
        val items = pageData.content.map(mapper::toSummary)

        return PageResponse(
            items = items,
            page = pageData.number,
            size = pageData.size,
            totalElements = pageData.totalElements,
            totalPages = pageData.totalPages,
            hasNext = pageData.hasNext()
        )
    }

    private fun <T> and(base: Specification<T>?, add: Specification<T>?): Specification<T>? {
        if (add == null) return base
        return base?.and(add) ?: add
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): ListingResponse {
        val listingFound = repo.findById(id).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id$id not found")
        return mapper.toOneResponse(listingFound)
    }

    @Transactional
    override fun create(req: ListingCreateRequest): UUID {
        val entity = mapper.requestToEntity(req)
        repo.save(entity)
        return entity.id ?: throw IllegalStateException("Entity has no id after save")
    }

    @Transactional
    override fun update(id: UUID, req: ListingUpdateRequest): ListingResponse {
        val listingEntity = repo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found") }

        val currentStatus = listingEntity.status
        val requestStatus = req.status

        if (requestStatus != null && requestStatus != currentStatus) {
            assertStatusTransition(currentStatus, requestStatus)
        }

        mapper.applyUpdate(req, listingEntity)
        repo.save(listingEntity)

        return mapper.toOneResponse(listingEntity)
    }

    private fun assertStatusTransition(from: ListingStatus, to: ListingStatus) {
        when (from) {
            ListingStatus.ACTIVE -> {
                if (to != ListingStatus.SOLD && to != ListingStatus.ARCHIVED) conflict(from, to)
            }
            ListingStatus.SOLD -> {
                if (to != ListingStatus.ARCHIVED) conflict(from, to)
            }
            ListingStatus.ARCHIVED -> conflict(from, to)
        }
    }

    private fun conflict(from: ListingStatus, to: ListingStatus): Nothing {
        throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Status transition not allowed: $from → $to"
        )
    }

    override fun archive(id: UUID) {
        throw UnsupportedOperationException("not implemented yet")
    }
}
