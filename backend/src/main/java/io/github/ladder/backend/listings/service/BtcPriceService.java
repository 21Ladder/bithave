package io.github.ladder.backend.listings.service;


import io.github.ladder.backend.listings.dto.BtcPriceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
public class BtcPriceService {

    @Value("${coingecko.apiKey}")
    private String apiKey;

    private final WebClient webClient;

    public BtcPriceService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.coingecko.com/api/v3").build();
    }

    public BtcPriceResponse fetchCurrentPrice() {
        String url = "/simple/price?ids=bitcoin&vs_currencies=eur&include_last_updated_at=true&x_cg_demo_api_key=" + apiKey;

        BtcPriceResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BtcPriceResponse.class)
                .block();

        BigDecimal price = response.bitcoin.eur;               // ✅ BTC price in EUR
        long timestamp = response.bitcoin.last_updated_at;     // ✅ UNIX timestamp

        return response;
    }

}
