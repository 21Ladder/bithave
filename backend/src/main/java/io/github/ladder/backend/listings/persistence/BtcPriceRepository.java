package io.github.ladder.backend.listings.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BtcPriceRepository extends JpaRepository<BtcPrice, Long> {
}
