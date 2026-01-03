package io.github.ladder.backend.listings.service

import io.github.ladder.backend.listings.dto.BtcPriceResponse
import io.github.ladder.backend.listings.persistence.BtcPrice
import io.github.ladder.backend.listings.persistence.BtcPriceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PriceUpdaterTest {

    @Mock
    private lateinit var btcPriceService: BtcPriceService

    @Mock
    private lateinit var btcPriceRepository: BtcPriceRepository

    private lateinit var priceUpdater: PriceUpdater

    @BeforeEach
    fun setUp() {
        priceUpdater = PriceUpdater(btcPriceService, btcPriceRepository)
    }

    @Test
    fun updatePrice_savesFetchedValue() {
        val response = BtcPriceResponse(BtcPriceResponse.Bitcoin(BigDecimal("27500.50"), 123456789L))

        `when`(btcPriceService.fetchCurrentPrice()).thenReturn(response)
        `when`(btcPriceRepository.findById(1L)).thenReturn(Optional.empty())

        priceUpdater.updatePrice()

        val captor = ArgumentCaptor.forClass(BtcPrice::class.java)
        verify(btcPriceRepository).save(captor.capture())

        assertThat(captor.value.priceEuro).isEqualByComparingTo("27500.50")
    }

    @Test
    fun updatePrice_swallowExceptions() {
        `when`(btcPriceService.fetchCurrentPrice()).thenThrow(RuntimeException("Service unavailable"))

        priceUpdater.updatePrice()

        verifyNoInteractions(btcPriceRepository)
    }
}
