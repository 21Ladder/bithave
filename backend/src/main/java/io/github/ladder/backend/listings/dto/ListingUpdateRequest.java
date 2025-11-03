package io.github.ladder.backend.listings.dto;

import io.github.ladder.backend.listings.domain.ListingStatus;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public class ListingUpdateRequest {
    public String title;
    @PositiveOrZero public Long priceUsd;
    @PositiveOrZero public Integer quantity;
    public String categoryPath;
    public ListingStatus status;
    public List<String> images;
}
