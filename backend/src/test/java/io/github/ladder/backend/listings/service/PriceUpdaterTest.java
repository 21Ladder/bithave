package io.github.ladder.backend.listings.service;

import io.github.ladder.backend.listings.dto.BtcPriceResponse;
import io.github.ladder.backend.listings.persistence.BtcPrice;
import io.github.ladder.backend.listings.persistence.BtcPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceUpdaterTest {

    @Mock
    private BtcPriceService btcPriceService;
    @Mock
    private BtcPriceRepository btcPriceRepository;

    private PriceUpdater priceUpdater;

    @BeforeEach
    void setUp() {
        priceUpdater = new PriceUpdater(btcPriceService, btcPriceRepository);
    }

    // verifies that a fetched BTC price is saved to the repository
    @Test
    void updatePrice_savesFetchedValue() {
        BtcPriceResponse.Bitcoin bitcoin = new BtcPriceResponse.Bitcoin();
        bitcoin.eur = new BigDecimal("27500.50");
        bitcoin.last_updated_at = 123456789L;
        BtcPriceResponse response = new BtcPriceResponse();
        response.bitcoin = bitcoin;

        when(btcPriceService.fetchCurrentPrice()).thenReturn(response);
        when(btcPriceRepository.findById(1L)).thenReturn(Optional.empty());

        priceUpdater.updatePrice();

        ArgumentCaptor<BtcPrice> captor = ArgumentCaptor.forClass(BtcPrice.class);
        verify(btcPriceRepository).save(captor.capture());

        assertThat(captor.getValue().getPriceEuro()).isEqualByComparingTo("27500.50");
    }

    // verifies that failures in btcPriceService do not attempt to save data
    @Test
    void updatePrice_swallowExceptions() {
        when(btcPriceService.fetchCurrentPrice()).thenThrow(new RuntimeException("Service unavailable"));

        priceUpdater.updatePrice();

        verify(btcPriceRepository, never()).save(any(BtcPrice.class));
    }
}
