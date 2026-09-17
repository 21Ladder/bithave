package io.github.ladder.backend.orders.service

import io.github.ladder.backend.orders.persistence.ProcessedWebhookEvent
import io.github.ladder.backend.orders.persistence.ProcessedWebhookEventRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

// Separate bean so REQUIRES_NEW actually applies, a self-invocation would bypass the proxy
@Service
class WebhookDeduplicator(
    private val processedEvents: ProcessedWebhookEventRepository
) {

    // Returns true only for the first delivery of an event
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun claim(deliveryId: String, eventType: String): Boolean {
        if (processedEvents.existsById(deliveryId)) return false
        return try {
            processedEvents.saveAndFlush(ProcessedWebhookEvent(deliveryId, eventType))
            true
        } catch (ex: DataIntegrityViolationException) {
            // A concurrent delivery of the same event won the race
            false
        }
    }
}
