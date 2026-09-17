package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.BtcPayProperties
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.persistence.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

// Safety net for lost webhooks. Without it a paid order would sit pending forever.
@Service
class PaymentStatusPoller(
    private val orderRepository: OrderRepository,
    private val settlementService: PaymentSettlementService,
    private val btcPayClient: BtcPayClient,
    private val btcPayProperties: BtcPayProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 120_000, initialDelay = 60_000)
    fun reconcilePendingOrders() {
        val cutoff = Instant.now().minus(Duration.ofMinutes(btcPayProperties.invoiceExpirationMinutes.toLong()))
        val stale = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, cutoff)
        if (stale.isEmpty()) return

        log.info("Reconciling {} pending order(s) against BTCPay", stale.size)
        stale.forEach { order ->
            val invoiceId = order.btcpayInvoiceId ?: return@forEach
            try {
                when (btcPayClient.getInvoice(invoiceId).status) {
                    "Settled", "Complete" -> settlementService.settleFromInvoice(invoiceId)
                    "Expired" -> settlementService.releaseFromInvoice(invoiceId, OrderStatus.EXPIRED)
                    "Invalid" -> settlementService.releaseFromInvoice(invoiceId, OrderStatus.CANCELLED)
                    else -> log.debug("Order {} still pending at BTCPay", order.orderNumber)
                }
            } catch (ex: Exception) {
                log.warn("Could not reconcile order {}", order.orderNumber, ex)
            }
        }
    }
}
