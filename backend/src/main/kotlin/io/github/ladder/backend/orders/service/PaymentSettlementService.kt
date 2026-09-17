package io.github.ladder.backend.orders.service

import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.orders.domain.CartStatus
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.persistence.CartRepository
import io.github.ladder.backend.orders.persistence.OrderEntity
import io.github.ladder.backend.orders.persistence.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

// Applies a BTCPay invoice outcome to an order. Shared by the webhook and the fallback poller.
@Service
class PaymentSettlementService(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val listingRepository: ListingRepository,
    private val btcPayClient: BtcPayClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // Verifies the amount against BTCPay itself, the webhook payload is never trusted
    @Transactional
    fun settleFromInvoice(invoiceId: String) {
        val order = orderRepository.findWithItemsByBtcpayInvoiceId(invoiceId).orElse(null)
        if (order == null) {
            log.warn("No order found for BTCPay invoice {}", invoiceId)
            return
        }
        if (order.status != OrderStatus.PENDING_PAYMENT) {
            log.debug("Order {} already in status {}, nothing to settle", order.orderNumber, order.status)
            return
        }

        val invoice = btcPayClient.getInvoice(invoiceId)
        val expected = BigDecimal.valueOf(order.totalCents).movePointLeft(2)
        val actual = invoice.amount

        if (actual == null || actual.compareTo(expected) != 0 || !order.currency.equals(invoice.currency, true)) {
            log.warn(
                "Amount mismatch for order {}: expected {} {}, invoice reports {} {}",
                order.orderNumber, expected, order.currency, actual, invoice.currency
            )
            return
        }

        order.transitionTo(OrderStatus.PAID)
        consumeReservations(order)
        orderRepository.save(order)
        log.info("Order {} settled", order.orderNumber)
    }

    // Invoice expired or went invalid, give the stock back
    @Transactional
    fun releaseFromInvoice(invoiceId: String, target: OrderStatus) {
        val order = orderRepository.findWithItemsByBtcpayInvoiceId(invoiceId).orElse(null)
        if (order == null) {
            log.warn("No order found for BTCPay invoice {}", invoiceId)
            return
        }
        if (order.status != OrderStatus.PENDING_PAYMENT) {
            return
        }

        order.transitionTo(target)
        releaseReservations(order)
        orderRepository.save(order)
        log.info("Order {} moved to {} and released its reservations", order.orderNumber, target)
    }

    // Paid: the reservation becomes a real sale, so stock drops with it
    private fun consumeReservations(order: OrderEntity) {
        order.items.forEach { item ->
            val listing = listingRepository.findByIdForUpdate(item.listingId).orElse(null)
            if (listing == null) {
                log.warn("Listing {} vanished while settling order {}", item.listingId, order.orderNumber)
                return@forEach
            }
            val amount = minOf(item.quantity, listing.reservedQuantity)
            listing.decreaseReserved(amount)
            listing.quantity = (listing.quantity - amount).coerceAtLeast(0)
            listingRepository.save(listing)
        }
        closeCart(order, CartStatus.CONVERTED)
    }

    private fun releaseReservations(order: OrderEntity) {
        order.items.forEach { item ->
            val listing = listingRepository.findByIdForUpdate(item.listingId).orElse(null) ?: return@forEach
            listing.decreaseReserved(minOf(item.quantity, listing.reservedQuantity))
            listingRepository.save(listing)
        }
        closeCart(order, CartStatus.EXPIRED)
    }

    private fun closeCart(order: OrderEntity, status: CartStatus) {
        order.cart?.let {
            it.status = status
            cartRepository.save(it)
        }
    }
}
