package io.github.ladder.backend.orders.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedWebhookEventRepository : JpaRepository<ProcessedWebhookEvent, String>
