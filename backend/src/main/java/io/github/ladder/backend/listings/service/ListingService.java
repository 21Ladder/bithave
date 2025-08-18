package io.github.ladder.backend.listings.service;

import io.github.ladder.backend.listings.dto.ListingCreateRequest;
import io.github.ladder.backend.listings.dto.ListingUpdateRequest;

public interface ListingService {

    void list();

    void getById(long id);

    void create(ListingCreateRequest req);

    void update(ListingUpdateRequest req);

    void archive(long id);
}
