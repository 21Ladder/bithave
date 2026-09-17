package io.github.ladder.backend.orders.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

// One row per BTCPay delivery, the primary key makes replays a no-op
@Entity
@Table(name = "processed_webhook_events")
class ProcessedWebhookEvent() {

    @Id
    @Column(name = "delivery_id", length = 128)
    var deliveryId: String = ""

    @Column(name = "event_type", nullable = false, length = 64)
    var eventType: String = ""

    @Column(name = "received_at", nullable = false)
    var receivedAt: Instant = Instant.now()

    constructor(deliveryId: String, eventType: String) : this() {
        this.deliveryId = deliveryId
        this.eventType = eventType
    }
}
