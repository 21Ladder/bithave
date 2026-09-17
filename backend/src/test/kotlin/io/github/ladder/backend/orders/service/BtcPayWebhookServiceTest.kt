package io.github.ladder.backend.orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.ladder.backend.orders.domain.OrderStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class BtcPayWebhookServiceTest {

    private lateinit var settlementService: PaymentSettlementService
    private lateinit var deduplicator: WebhookDeduplicator
    private lateinit var service: BtcPayWebhookService

    @BeforeEach
    fun setUp() {
        settlementService = mock(PaymentSettlementService::class.java)
        deduplicator = mock(WebhookDeduplicator::class.java)
        service = BtcPayWebhookService(ObjectMapper(), settlementService, deduplicator)
    }

    @Test
    fun settledEventTriggersSettlement() {
        `when`(deduplicator.claim("d1", "InvoiceSettled")).thenReturn(true)

        service.handle("""{"deliveryId":"d1","type":"InvoiceSettled","invoiceId":"INV-1"}""")

        verify(settlementService, times(1)).settleFromInvoice("INV-1")
    }

    @Test
    fun replayedDeliveryIsIgnored() {
        `when`(deduplicator.claim("d1", "InvoiceSettled")).thenReturn(false)

        service.handle("""{"deliveryId":"d1","type":"InvoiceSettled","invoiceId":"INV-1"}""")

        verify(settlementService, never()).settleFromInvoice("INV-1")
    }

    @Test
    fun expiredEventReleasesTheOrder() {
        `when`(deduplicator.claim("d2", "InvoiceExpired")).thenReturn(true)

        service.handle("""{"deliveryId":"d2","type":"InvoiceExpired","invoiceId":"INV-2"}""")

        verify(settlementService).releaseFromInvoice("INV-2", OrderStatus.EXPIRED)
    }

    @Test
    fun unknownEventTypeIsClaimedButNotActedOn() {
        `when`(deduplicator.claim("d3", "InvoiceReceivedPayment")).thenReturn(true)

        service.handle("""{"deliveryId":"d3","type":"InvoiceReceivedPayment","invoiceId":"INV-3"}""")

        verify(settlementService, never()).settleFromInvoice("INV-3")
    }

    @Test
    fun payloadWithoutInvoiceIdIsIgnored() {
        service.handle("""{"deliveryId":"d4","type":"InvoiceSettled"}""")

        verify(deduplicator, never()).claim("d4", "InvoiceSettled")
    }

    @Test
    fun unknownFieldsInThePayloadDoNotBreakParsing() {
        `when`(deduplicator.claim("d5", "InvoiceSettled")).thenReturn(true)

        service.handle(
            """{"deliveryId":"d5","type":"InvoiceSettled","invoiceId":"INV-5",
               "somethingNew":{"nested":true},"timestamp":1234567}"""
        )

        verify(settlementService).settleFromInvoice("INV-5")
    }
}
