package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.AppProperties
import io.github.ladder.backend.config.BtcPayProperties
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.dto.CheckoutRequest
import io.github.ladder.backend.orders.dto.CheckoutResponse
import io.github.ladder.backend.orders.persistence.OrderEntity
import io.github.ladder.backend.orders.persistence.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

// Turns a cart into an order and hands the buyer a BTCPay invoice to pay
@Service
class CheckoutService(
    private val orderRepository: OrderRepository,
    private val orderTransactions: OrderTransactions,
    private val btcPayClient: BtcPayClient,
    private val btcPayProperties: BtcPayProperties,
    private val appProperties: AppProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun checkout(request: CheckoutRequest): CheckoutResponse {
        val cartId = request.cartId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cartId is required")

        // A pending order for this cart means the buyer clicked twice, reuse its invoice
        val existing = orderRepository.findFirstByCart_IdAndStatus(cartId, OrderStatus.PENDING_PAYMENT)
        if (existing.isPresent) {
            val order = existing.get()
            order.btcpayInvoiceId?.let { invoiceId ->
                val invoice = btcPayClient.getInvoice(invoiceId)
                return toResponse(order, invoice.checkoutLink)
            }
        }

        val order = orderTransactions.createPendingOrder(cartId, request)

        // Network call stays outside the transaction so a slow BTCPay holds no DB connection
        val invoice = try {
            btcPayClient.createInvoice(
                amount = BigDecimal.valueOf(order.totalCents).movePointLeft(2),
                currency = order.currency,
                metadata = mapOf(
                    "orderId" to order.id.toString(),
                    "orderNumber" to order.orderNumber,
                    "cartId" to cartId.toString()
                ),
                redirectUrl = "${appProperties.baseUrl.trimEnd('/')}/orders/${order.orderNumber}",
                expirationMinutes = btcPayProperties.invoiceExpirationMinutes
            )
        } catch (ex: BtcPayException) {
            orderTransactions.cancelOrderAndReleaseCart(order.id!!)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment provider unavailable", ex)
        }

        orderTransactions.attachInvoice(order.id!!, invoice.id)
        log.info("Created order {} with BTCPay invoice {}", order.orderNumber, invoice.id)

        return toResponse(order, invoice.checkoutLink)
    }

    private fun toResponse(order: OrderEntity, checkoutLink: String?) = CheckoutResponse(
        orderNumber = order.orderNumber,
        status = order.status.name,
        checkoutLink = checkoutLink,
        totalCents = order.totalCents,
        currency = order.currency
    )
}
