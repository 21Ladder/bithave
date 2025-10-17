package io.github.ladder.backend.listings.api;


import io.github.ladder.backend.listings.persistence.BtcPrice;
import io.github.ladder.backend.listings.persistence.BtcPriceRepository;
import io.github.ladder.backend.listings.service.BtcPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BtcPriceController {

    private final BtcPriceRepository btcPriceRepository;
    public BtcPriceController(BtcPriceRepository btcPriceRepository) {
        this.btcPriceRepository = btcPriceRepository;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @GetMapping("/btcPrice")
    public float getCurrentBtcPrice() {

        // fetches the current btc price from the database (gets updated every defined time)
        float btcPrice = btcPriceRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("BTC price not available yet"))
                .getPriceEuro()
                .floatValue();

        return btcPrice;
    }
}
