package io.github.ladder.backend.listings.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface BtcPriceRepository : JpaRepository<BtcPrice, Long>
