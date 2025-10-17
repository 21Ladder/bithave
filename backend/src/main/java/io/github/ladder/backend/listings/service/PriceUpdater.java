package io.github.ladder.backend.listings.service;

import io.github.ladder.backend.listings.dto.BtcPriceResponse;
import io.github.ladder.backend.listings.persistence.BtcPrice;
import io.github.ladder.backend.listings.persistence.BtcPriceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PriceUpdater {

    private final BtcPriceService btcPriceService;
    private final BtcPriceRepository btcPriceRepository;

    public PriceUpdater(BtcPriceService btcPriceService, BtcPriceRepository btcPriceRepository) {
        this.btcPriceService = btcPriceService;
        this.btcPriceRepository = btcPriceRepository;
    }

    @Scheduled(fixedRate = 1800000) // runs every 30 minutes right now
    public void updatePrice() {
        try {
            // this fetches the current btc price
            BtcPriceResponse response = btcPriceService.fetchCurrentPrice();

            //searches for the 1 entry in the table to overwrite
            BtcPrice entry = btcPriceRepository.findById(1L).orElse(new BtcPrice());
            entry.setId(1L);
            entry.setPriceEuro(response.bitcoin.eur);
            entry.setPriceTimestamp(Instant.now());
            btcPriceRepository.save(entry);

            System.out.println("Saved BTC price: " + response.bitcoin.eur);

        } catch (Exception e) {
            System.err.println("Error fetching BTC price: " + e.getMessage());
        }
    }
}

