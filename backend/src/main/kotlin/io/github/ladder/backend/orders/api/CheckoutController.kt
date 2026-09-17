package io.github.ladder.backend.orders.api

import io.github.ladder.backend.orders.dto.CheckoutRequest
import io.github.ladder.backend.orders.dto.CheckoutResponse
import io.github.ladder.backend.orders.dto.OrderResponse
import io.github.ladder.backend.orders.service.CheckoutService
import io.github.ladder.backend.orders.service.OrderQueryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// REST controller for placing an order and reading its payment status
@RestController
@RequestMapping("/api/v1")
class CheckoutController(
    private val checkoutService: CheckoutService,
    private val orderQueryService: OrderQueryService
) {

    // Turn the cart into an order and return the BTCPay checkout link
    @PostMapping("/checkout")
    fun checkout(@RequestBody @Valid request: CheckoutRequest): CheckoutResponse =
        checkoutService.checkout(request)

    // Polled by the confirmation page after the buyer returns from BTCPay
    @GetMapping("/orders/{orderNumber}")
    fun getOrder(@PathVariable orderNumber: String): OrderResponse =
        orderQueryService.getByOrderNumber(orderNumber)
}
