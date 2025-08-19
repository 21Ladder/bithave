package io.github.ladder.backend.listings.dto;

import java.util.UUID;

public record ListingQuery(
        int page,
        int size,
        String sort,
        String order,
        String q,
        String status,
        String category,
        Long minPriceSats,
        Long maxPriceSats,
        UUID sellerId
) {}