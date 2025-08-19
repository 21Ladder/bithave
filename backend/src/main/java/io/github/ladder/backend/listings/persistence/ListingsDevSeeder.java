package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

@Profile("dev")
@Configuration
public class ListingsDevSeeder {

    private final ListingRepository repo;

    public ListingsDevSeeder(ListingRepository repo) {
        this.repo = repo;
    }

    @Bean
    ApplicationRunner seedListings() {
        return args -> {
            if (repo.count() > 0) {
                return; // schon Daten vorhanden
            }

            ListingEntity l1 = new ListingEntity();
            l1.setTitle("Raspberry Pi 4 Model B 8GB");
            l1.setPriceSats(250_000L);
            l1.setStatus(ListingStatus.ACTIVE);
            l1.setSellerId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            l1.setImages(List.of(
                    "https://picsum.photos/id/1010/600/400",
                    "https://picsum.photos/id/1011/600/400"
            ));

            ListingEntity l2 = new ListingEntity();
            l2.setTitle("ThinkPad X1 Carbon (Gen 9)");
            l2.setPriceSats(3_900_000L);
            l2.setStatus(ListingStatus.ACTIVE);
            l2.setSellerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            l2.setImages(List.of(
                    "https://picsum.photos/id/1027/600/400"
            ));

            ListingEntity l3 = new ListingEntity();
            l3.setTitle("Nintendo Switch OLED, weiß");
            l3.setPriceSats(2_100_000L);
            l3.setStatus(ListingStatus.SOLD);
            l3.setSellerId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
            l3.setImages(List.of(
                    "https://picsum.photos/id/1050/600/400"
            ));

            ListingEntity l4 = new ListingEntity();
            l4.setTitle("Bosch Schlagbohrmaschine GSB 13 RE");
            l4.setPriceSats(600_000L);
            l4.setStatus(ListingStatus.ACTIVE);
            l4.setSellerId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
            l4.setImages(List.of(
                    "https://picsum.photos/id/1044/600/400"
            ));

            ListingEntity l5 = new ListingEntity();
            l5.setTitle("Apple iPhone 12, 128GB, gebraucht");
            l5.setPriceSats(3_100_000L);
            l5.setStatus(ListingStatus.ARCHIVED);
            l5.setSellerId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
            l5.setImages(List.of(
                    "https://picsum.photos/id/103/600/400"
            ));

            repo.saveAll(List.of(l1, l2, l3, l4, l5));
        };
    }
}
