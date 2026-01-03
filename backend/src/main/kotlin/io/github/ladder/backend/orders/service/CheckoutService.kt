package io.github.ladder.backend.orders.service

import io.github.ladder.backend.orders.dto.CheckoutResponse
import io.github.ladder.backend.orders.persistence.CartRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

// Service for handling the checkout process, including creating invoices via BTCPay Server
@Service
class CheckoutService(
    private val cartRepository: CartRepository,
    private val btcPayClient: BtcPayClient
) {

    @Transactional(readOnly = true)
    fun createInvoice(cartId: UUID): CheckoutResponse {
        val cart = cartRepository.findWithItemsById(cartId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found") }

        val totalUsd = cart.items.sumOf { it.unitPriceUsd.toLong() * it.quantity }
        if (totalUsd <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart total must be greater than zero")
        }

        val metadata = mapOf(
            "cartId" to cart.id.toString()
        )

        val invoice = btcPayClient.createInvoice(
            amount = BigDecimal.valueOf(totalUsd.toLong()),
            currency = "USD",
            metadata = metadata
        )

        return CheckoutResponse(
            invoiceId = invoice.id,
            status = invoice.status,
            checkoutLink = invoice.checkoutLink,
            amount = invoice.amount?.toDouble(),
            currency = invoice.currency
        )
    }
}
