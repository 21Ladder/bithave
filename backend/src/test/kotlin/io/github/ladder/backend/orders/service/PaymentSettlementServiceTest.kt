package io.github.ladder.backend.orders.service

import io.github.ladder.backend.listings.persistence.CategoryEntity
import io.github.ladder.backend.listings.persistence.ListingEntity
import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.persistence.CartEntity
import io.github.ladder.backend.orders.persistence.CartRepository
import io.github.ladder.backend.orders.persistence.OrderEntity
import io.github.ladder.backend.orders.persistence.OrderItemEntity
import io.github.ladder.backend.orders.persistence.OrderRepository
import io.github.ladder.backend.orders.persistence.ShippingAddress
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class PaymentSettlementServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var cartRepository: CartRepository
    private lateinit var listingRepository: ListingRepository
    private lateinit var btcPayClient: BtcPayClient
    private lateinit var service: PaymentSettlementService

    private val invoiceId = "INV-1"
    private lateinit var listing: ListingEntity
    private lateinit var order: OrderEntity

    @BeforeEach
    fun setUp() {
        orderRepository = mock(OrderRepository::class.java)
        cartRepository = mock(CartRepository::class.java)
        listingRepository = mock(ListingRepository::class.java)
        btcPayClient = mock(BtcPayClient::class.java)
        service = PaymentSettlementService(orderRepository, cartRepository, listingRepository, btcPayClient)

        val listingId = UUID.randomUUID()
        listing = ListingEntity("Shrimp", 1_200, 10, CategoryEntity("livestock", null), listOf(), UUID.randomUUID())
        listing.id = listingId
        listing.increaseReserved(2)

        order = OrderEntity(
            orderNumber = "BH-260917-AAAAAA",
            cart = CartEntity(),
            email = "buyer@example.com",
            shippingAddress = ShippingAddress("Buyer", "Street 1", "8010", "Graz", "AT"),
            totalCents = 2_400,
            currency = "EUR"
        )
        order.id = UUID.randomUUID()
        order.btcpayInvoiceId = invoiceId
        order.addItem(OrderItemEntity(listingId, "Shrimp", 1_200, 2))

        `when`(orderRepository.findWithItemsByBtcpayInvoiceId(invoiceId)).thenReturn(Optional.of(order))
        `when`(listingRepository.findByIdForUpdate(listingId)).thenReturn(Optional.of(listing))
    }

    @Test
    fun settledInvoiceMarksOrderPaidAndTurnsReservationIntoASale() {
        `when`(btcPayClient.getInvoice(invoiceId)).thenReturn(invoice(BigDecimal("24.00"), "EUR"))

        service.settleFromInvoice(invoiceId)

        assertEquals(OrderStatus.PAID, order.status)
        assertEquals(8, listing.quantity)
        assertEquals(0, listing.reservedQuantity)
    }

    @Test
    fun underpaidInvoiceLeavesTheOrderPendingAndStockUntouched() {
        `when`(btcPayClient.getInvoice(invoiceId)).thenReturn(invoice(BigDecimal("12.00"), "EUR"))

        service.settleFromInvoice(invoiceId)

        assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
        assertEquals(10, listing.quantity)
        assertEquals(2, listing.reservedQuantity)
        verify(orderRepository, never()).save(order)
    }

    @Test
    fun wrongCurrencyIsRejectedEvenWhenTheNumberMatches() {
        `when`(btcPayClient.getInvoice(invoiceId)).thenReturn(invoice(BigDecimal("24.00"), "USD"))

        service.settleFromInvoice(invoiceId)

        assertEquals(OrderStatus.PENDING_PAYMENT, order.status)
    }

    @Test
    fun replayOfASettledOrderDoesNotDecrementStockTwice() {
        `when`(btcPayClient.getInvoice(invoiceId)).thenReturn(invoice(BigDecimal("24.00"), "EUR"))
        service.settleFromInvoice(invoiceId)

        service.settleFromInvoice(invoiceId)

        assertEquals(OrderStatus.PAID, order.status)
        assertEquals(8, listing.quantity)
    }

    @Test
    fun expiredInvoiceReleasesTheReservationWithoutTouchingStock() {
        service.releaseFromInvoice(invoiceId, OrderStatus.EXPIRED)

        assertEquals(OrderStatus.EXPIRED, order.status)
        assertEquals(10, listing.quantity)
        assertEquals(0, listing.reservedQuantity)
    }

    private fun invoice(amount: BigDecimal, currency: String) =
        BtcPayInvoiceResponse(invoiceId, "Settled", "http://btcpay/i/$invoiceId", amount, currency)
}
