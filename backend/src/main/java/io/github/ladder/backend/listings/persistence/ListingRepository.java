package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<ListingEntity, UUID>, JpaSpecificationExecutor<ListingEntity> {

    Page<ListingEntity> findByStatus(ListingStatus status, Pageable pageable);

    Page<ListingEntity> findByCategory_PathStartingWithIgnoreCase(String categoryPath, Pageable pageable);

    Page<ListingEntity> findByStatusAndCategory_PathStartingWithIgnoreCase(ListingStatus status, String categoryPath, Pageable pageable);
}