package io.github.ladder.backend.orders.service

import io.github.ladder.backend.orders.dto.OrderResponse
import io.github.ladder.backend.orders.mapper.OrderMapper
import io.github.ladder.backend.orders.persistence.OrderRepository
import io.github.ladder.backend.users.service.CurrentUserProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

// Read side for the confirmation page, which polls until the order is paid
@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper,
    private val currentUserProvider: CurrentUserProvider
) {

    @Transactional(readOnly = true)
    fun getByOrderNumber(orderNumber: String): OrderResponse {
        val order = orderRepository.findWithItemsByOrderNumber(orderNumber)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found") }
        return orderMapper.toResponse(order)
    }

    // Orders placed while signed in, newest first
    @Transactional(readOnly = true)
    fun listForCurrentUser(): List<OrderResponse> {
        val user = currentUserProvider.require()
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(user.id!!)
            .map { orderMapper.toResponse(it) }
    }
}
