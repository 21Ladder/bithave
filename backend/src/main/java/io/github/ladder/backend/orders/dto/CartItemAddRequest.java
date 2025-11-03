package io.github.ladder.backend.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemAddRequest(
        @NotNull UUID listingId,
        @Min(1) Integer quantity
) {}
