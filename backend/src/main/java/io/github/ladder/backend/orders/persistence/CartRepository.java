package io.github.ladder.backend.orders.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

    @EntityGraph(attributePaths = {"items", "items.listing"})
    Optional<CartEntity> findWithItemsById(UUID id);
}
