package io.github.ladder.backend.orders.dto;

import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID listingId,
        String title,
        String thumbnailUrl,
        long unitPriceUsd,
        int quantity,
        long subtotalUsd
) {}
