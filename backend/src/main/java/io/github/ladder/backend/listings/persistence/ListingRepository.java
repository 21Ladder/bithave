package io.github.ladder.backend.listings.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListingRepository extends JpaRepository<ListingEntity, UUID> {

}
