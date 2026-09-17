package io.github.ladder.backend.orders.api

import io.github.ladder.backend.orders.dto.OrderResponse
import io.github.ladder.backend.orders.service.OrderQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// Order history of the signed in account
@RestController
@RequestMapping("/api/v1/account")
class AccountOrdersController(
    private val orderQueryService: OrderQueryService
) {

    @GetMapping("/orders")
    fun myOrders(): List<OrderResponse> = orderQueryService.listForCurrentUser()
}
