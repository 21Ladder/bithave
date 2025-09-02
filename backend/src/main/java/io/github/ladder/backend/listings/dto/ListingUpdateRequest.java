package io.github.ladder.backend.listings.dto;

import io.github.ladder.backend.listings.domain.ListingStatus;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public class ListingUpdateRequest {
    public String title;
    @PositiveOrZero public Long priceSats;
    public String categoryPath;
    public ListingStatus status;
    public List<String> images;
}
