package io.github.ladder.backend.orders.dto;

import jakarta.validation.constraints.Min;

public record CartItemUpdateRequest(
        @Min(1) int quantity
) {}
