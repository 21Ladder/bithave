package io.github.ladder.backend.listings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;

public class ListingCreateRequest {
    @NotBlank public String title;
    @NotNull @PositiveOrZero public Long priceSats;
    public String categoryPath;
    public List<String> images;
    @NotNull public UUID sellerId;
}
