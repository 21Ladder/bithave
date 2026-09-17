package io.github.ladder.backend.listings.api

import io.github.ladder.backend.listings.domain.ListingStatus
import io.github.ladder.backend.listings.dto.ListingQuery
import io.github.ladder.backend.listings.dto.ListingSummary
import io.github.ladder.backend.listings.dto.PageResponse
import io.github.ladder.backend.listings.service.ListingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import io.github.ladder.backend.users.service.CurrentUserProvider
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFailsWith

class ListingsControllerTest {

    private lateinit var listingService: RecordingListingService
    private lateinit var controller: ListingsController

    @BeforeEach
    fun setUp() {
        listingService = RecordingListingService()
        controller = ListingsController(listingService, mock(CurrentUserProvider::class.java))
    }

    @Test
    fun list_clampsAndNormalizesParameters() {
        val sellerId = UUID.randomUUID()
        val expected = PageResponse(
            items = listOf(
                ListingSummary(
                    id = UUID.randomUUID(),
                    title = "Title",
                    priceCents = 1000L,
                    categoryPath = "root/category",
                    status = ListingStatus.ACTIVE,
                    thumbnailUrl = "thumb.jpg",
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    availableQuantity = 5
                )
            ),
            page = 0,
            size = 10,
            totalElements = 1L,
            totalPages = 1,
            hasNext = false
        )

        listingService.response = expected

        val result = controller.list(
            -5,
            200,
            "createdAt",
            "ASC",
            "  status  ",
            "   q   ",
            "  category  ",
            -100L,
            50L,
            sellerId
        )

        assertThat(result).isSameAs(expected)

        val sentQuery = requireNotNull(listingService.lastQuery)

        assertThat(sentQuery.page).isZero()
        assertThat(sentQuery.size).isEqualTo(100)
        assertThat(sentQuery.sort).isEqualTo("createdAt")
        assertThat(sentQuery.order).isEqualTo("ASC")
        assertThat(sentQuery.status).isEqualTo("status")
        assertThat(sentQuery.q).isEqualTo("q")
        assertThat(sentQuery.category).isEqualTo("category")
        assertThat(sentQuery.minPriceCents).isNull()
        assertThat(sentQuery.maxPriceCents).isEqualTo(50L)
        assertThat(sentQuery.sellerId).isEqualTo(sellerId)
    }

    @Test
    fun list_throwsWhenMinGreaterThanMax() {
        assertFailsWith<IllegalArgumentException> {
            controller.list(
                0,
                20,
                "createdAt",
                "DESC",
                null,
                null,
                null,
                200L,
                100L,
                null
            )
        }

        assertThat(listingService.called).isFalse()
    }

    private class RecordingListingService : ListingService {
        var response: PageResponse<ListingSummary>? = null
        var lastQuery: ListingQuery? = null
        var called: Boolean = false

        override fun list(q: ListingQuery): PageResponse<ListingSummary> {
            called = true
            lastQuery = q
            return response ?: throw IllegalStateException("response not set")
        }

        override fun getById(id: UUID) = throw UnsupportedOperationException()
        override fun create(req: io.github.ladder.backend.listings.dto.ListingCreateRequest, sellerId: UUID) = throw UnsupportedOperationException()
        override fun update(id: UUID, req: io.github.ladder.backend.listings.dto.ListingUpdateRequest) = throw UnsupportedOperationException()
        override fun archive(id: UUID) = throw UnsupportedOperationException()
    }
}
