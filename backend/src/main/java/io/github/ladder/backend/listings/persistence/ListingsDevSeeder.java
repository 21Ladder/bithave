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

            ListingEntity l6 = new ListingEntity();
            l6.setTitle("Samsung Galaxy S21, 256GB, schwarz");
            l6.setPriceSats(3_500_000L);
            l6.setStatus(ListingStatus.ACTIVE);
            l6.setSellerId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
            l6.setImages(List.of("https://picsum.photos/id/1010/600/400"));

            ListingEntity l7 = new ListingEntity();
            l7.setTitle("Sony WH-1000XM4 Kopfhörer, Noise Cancelling");
            l7.setPriceSats(1_200_000L);
            l7.setStatus(ListingStatus.ACTIVE);
            l7.setSellerId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
            l7.setImages(List.of("https://picsum.photos/id/1011/600/400"));

            ListingEntity l8 = new ListingEntity();
            l8.setTitle("Dell XPS 13, 16GB RAM, 512GB SSD, silber");
            l8.setPriceSats(9_500_000L);
            l8.setStatus(ListingStatus.ARCHIVED);
            l8.setSellerId(UUID.fromString("88888888-8888-8888-8888-888888888888"));
            l8.setImages(List.of("https://picsum.photos/id/1012/600/400"));

            ListingEntity l9 = new ListingEntity();
            l9.setTitle("PlayStation 5 mit Controller");
            l9.setPriceSats(6_800_000L);
            l9.setStatus(ListingStatus.ACTIVE);
            l9.setSellerId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
            l9.setImages(List.of("https://picsum.photos/id/1013/600/400"));

            ListingEntity l10 = new ListingEntity();
            l10.setTitle("Nintendo Switch OLED, weiß");
            l10.setPriceSats(4_200_000L);
            l10.setStatus(ListingStatus.ACTIVE);
            l10.setSellerId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            l10.setImages(List.of("https://picsum.photos/id/1014/600/400"));

            ListingEntity l11 = new ListingEntity();
            l11.setTitle("Canon EOS 250D Spiegelreflexkamera");
            l11.setPriceSats(5_300_000L);
            l11.setStatus(ListingStatus.ARCHIVED);
            l11.setSellerId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
            l11.setImages(List.of("https://picsum.photos/id/1015/600/400"));

            ListingEntity l12 = new ListingEntity();
            l12.setTitle("GoPro Hero 9 Actionkamera");
            l12.setPriceSats(2_700_000L);
            l12.setStatus(ListingStatus.ACTIVE);
            l12.setSellerId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
            l12.setImages(List.of("https://picsum.photos/id/1016/600/400"));

            ListingEntity l13 = new ListingEntity();
            l13.setTitle("Apple MacBook Pro 14\", M1 Pro, 16GB RAM");
            l13.setPriceSats(25_000_000L);
            l13.setStatus(ListingStatus.ACTIVE);
            l13.setSellerId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
            l13.setImages(List.of("https://picsum.photos/id/1017/600/400"));

            ListingEntity l14 = new ListingEntity();
            l14.setTitle("HP LaserJet Pro Drucker");
            l14.setPriceSats(1_100_000L);
            l14.setStatus(ListingStatus.ARCHIVED);
            l14.setSellerId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
            l14.setImages(List.of("https://picsum.photos/id/1018/600/400"));

            ListingEntity l15 = new ListingEntity();
            l15.setTitle("Logitech MX Master 3S Maus");
            l15.setPriceSats(450_000L);
            l15.setStatus(ListingStatus.ACTIVE);
            l15.setSellerId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
            l15.setImages(List.of("https://picsum.photos/id/1019/600/400"));

            ListingEntity l16 = new ListingEntity();
            l16.setTitle("Razer BlackWidow V3 Mechanische Tastatur");
            l16.setPriceSats(650_000L);
            l16.setStatus(ListingStatus.ACTIVE);
            l16.setSellerId(UUID.fromString("12121212-1212-1212-1212-121212121212"));
            l16.setImages(List.of("https://picsum.photos/id/1020/600/400"));

            ListingEntity l17 = new ListingEntity();
            l17.setTitle("LG 27\" 4K UHD Monitor");
            l17.setPriceSats(3_000_000L);
            l17.setStatus(ListingStatus.ARCHIVED);
            l17.setSellerId(UUID.fromString("13131313-1313-1313-1313-131313131313"));
            l17.setImages(List.of("https://picsum.photos/id/1021/600/400"));

            ListingEntity l18 = new ListingEntity();
            l18.setTitle("Kindle Paperwhite 11. Generation");
            l18.setPriceSats(800_000L);
            l18.setStatus(ListingStatus.ACTIVE);
            l18.setSellerId(UUID.fromString("14141414-1414-1414-1414-141414141414"));
            l18.setImages(List.of("https://picsum.photos/id/1022/600/400"));

            ListingEntity l19 = new ListingEntity();
            l19.setTitle("Xiaomi E-Scooter Pro 2");
            l19.setPriceSats(7_200_000L);
            l19.setStatus(ListingStatus.ACTIVE);
            l19.setSellerId(UUID.fromString("15151515-1515-1515-1515-151515151515"));
            l19.setImages(List.of("https://picsum.photos/id/1023/600/400"));

            ListingEntity l20 = new ListingEntity();
            l20.setTitle("Dyson V11 Akkustaubsauger");
            l20.setPriceSats(6_500_000L);
            l20.setStatus(ListingStatus.ARCHIVED);
            l20.setSellerId(UUID.fromString("16161616-1616-1616-1616-161616161616"));
            l20.setImages(List.of("https://picsum.photos/id/1024/600/400"));

            repo.saveAll(List.of(l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12, l13, l14, l15, l16, l17, l18, l19, l20));
        };
    }
}
