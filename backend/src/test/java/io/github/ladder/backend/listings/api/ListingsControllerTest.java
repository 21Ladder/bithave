package io.github.ladder.backend.listings.api;

import io.github.ladder.backend.listings.domain.ListingStatus;
import io.github.ladder.backend.listings.dto.ListingQuery;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.PageResponse;
import io.github.ladder.backend.listings.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingsControllerTest {

    @Mock
    private ListingService listingService;

    private ListingsController controller;

    @BeforeEach
    void setUp() {
        controller = new ListingsController(listingService);
    }

    // ensures the controller clamps incoming values and strips whitespace before delegating
    @Test
    void list_clampsAndNormalizesParameters() {
        UUID sellerId = UUID.randomUUID();
        PageResponse<ListingSummary> expected = new PageResponse<>(
                List.of(new ListingSummary(
                        UUID.randomUUID(),
                        "Title",
                        1000L,
                        "root/category",
                        ListingStatus.ACTIVE,
                        "thumb.jpg",
                        Instant.parse("2024-01-01T00:00:00Z"),
                        5
                )),
                0,
                10,
                1L,
                1,
                false
        );

        when(listingService.list(org.mockito.ArgumentMatchers.any(ListingQuery.class))).thenReturn(expected);

        PageResponse<ListingSummary> result = controller.list(
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
        );

        assertThat(result).isSameAs(expected);

        ArgumentCaptor<ListingQuery> captor = ArgumentCaptor.forClass(ListingQuery.class);
        verify(listingService).list(captor.capture());
        ListingQuery sentQuery = captor.getValue();

        assertThat(sentQuery.page()).isZero();
        assertThat(sentQuery.size()).isEqualTo(100);
        assertThat(sentQuery.sort()).isEqualTo("createdAt");
        assertThat(sentQuery.order()).isEqualTo("ASC");
        assertThat(sentQuery.status()).isEqualTo("status");
        assertThat(sentQuery.q()).isEqualTo("q");
        assertThat(sentQuery.category()).isEqualTo("category");
        assertThat(sentQuery.minPriceSats()).isNull();
        assertThat(sentQuery.maxPriceSats()).isEqualTo(50L);
        assertThat(sentQuery.sellerId()).isEqualTo(sellerId);
    }

    // ensures invalid price filters instantly trigger bad request handling
    @Test
    void list_throwsWhenMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () ->
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
        );

        verifyNoInteractions(listingService);
    }
}
