package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<ListingEntity, UUID>, JpaSpecificationExecutor<ListingEntity> {

    Page<ListingEntity> findByStatus(ListingStatus status, Pageable pageable);
}