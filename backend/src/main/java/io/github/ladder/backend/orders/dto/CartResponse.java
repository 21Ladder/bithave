package io.github.ladder.backend.orders.dto;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        List<CartItemResponse> items,
        int totalQuantity,
        long totalPriceUsd
) {}
