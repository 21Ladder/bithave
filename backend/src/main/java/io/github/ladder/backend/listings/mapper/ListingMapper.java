package io.github.ladder.backend.listings.mapper;

import io.github.ladder.backend.listings.dto.ListingCreateRequest;
import io.github.ladder.backend.listings.dto.ListingResponse;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.ListingUpdateRequest;
import io.github.ladder.backend.listings.persistence.ListingEntity;

public interface ListingMapper {

    // Für GET /listings (Listenansicht)
    ListingSummary toSummary(ListingEntity entity);

    // Für Create/Update/Detail
    ListingEntity toEntity(ListingCreateRequest req);
    void applyUpdate(ListingUpdateRequest req, ListingEntity target);
    ListingResponse toResponse(ListingEntity entity);
}
