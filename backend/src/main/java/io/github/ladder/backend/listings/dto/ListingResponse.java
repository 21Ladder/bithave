package io.github.ladder.backend.listings.dto;

import io.github.ladder.backend.listings.domain.ListingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListingResponse(
        UUID id,
        String title,
        long priceSats,
        ListingStatus status,
        List<String> images,
        UUID sellerId,
        Instant createdAt,
        Instant updatedAt
) {}
