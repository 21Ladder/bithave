package io.github.ladder.backend.listings.dto;

import io.github.ladder.backend.listings.domain.ListingStatus;

import java.time.Instant;
import java.util.UUID;

public record ListingSummary(
        UUID id,
        String title,
        long priceSats,
        ListingStatus status,
        String thumbnailUrl,
        Instant createdAt
) {
}
