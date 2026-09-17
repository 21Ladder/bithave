package io.github.ladder.backend.orders.mapper

import io.github.ladder.backend.orders.dto.OrderItemResponse
import io.github.ladder.backend.orders.dto.OrderResponse
import io.github.ladder.backend.orders.persistence.OrderEntity
import org.springframework.stereotype.Component

// Mapper to convert OrderEntity to OrderResponse
@Component
class OrderMapper {

    fun toResponse(order: OrderEntity, checkoutLink: String? = null): OrderResponse =
        OrderResponse(
            orderNumber = order.orderNumber,
            status = order.status.name,
            email = order.email,
            items = order.items.map { toItemResponse(it) },
            totalCents = order.totalCents,
            currency = order.currency,
            checkoutLink = checkoutLink,
            createdAt = order.createdAt,
            paidAt = order.paidAt
        )

    private fun toItemResponse(item: io.github.ladder.backend.orders.persistence.OrderItemEntity) =
        OrderItemResponse(
            listingId = item.listingId,
            title = item.titleSnapshot,
            unitPriceCents = item.unitPriceCents,
            quantity = item.quantity,
            subtotalCents = item.subtotalCents
        )
}
