package io.github.ladder.backend.listings.service;

import io.github.ladder.backend.listings.dto.*;
import java.util.UUID;

public interface ListingService {
    PageResponse<ListingSummary> list(ListingQuery q);
    ListingResponse getById(UUID id);
    UUID create(ListingCreateRequest req);
    ListingResponse update(UUID id, ListingUpdateRequest req);
    void archive(UUID id);
}
