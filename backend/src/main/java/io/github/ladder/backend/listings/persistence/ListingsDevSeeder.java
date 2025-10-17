// backend/src/main/java/io/github/ladder/backend/listings/persistence/DevSeeders.java
package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Profile("dev")
@Configuration
public class ListingsDevSeeder{

    // ---------- 1) Kategorien ----------

    @Bean
    @Order(1)
    ApplicationRunner seedCategories(CategoryRepository categories) {
        return args -> {
            if (categories.count() > 0) return;

            // fixe Paths (du kannst die Liste beliebig erweitern)
            List<String> paths = List.of(
                    // electronics (kept small)
                    "electronics",
                    "electronics/phones",
                    "electronics/tv",
                    "electronics/audio",

                    // computers
                    "computers",
                    "computers/laptops",
                    "computers/desktops",
                    "computers/components",

                    // vehicles
                    "vehicles",
                    "vehicles/cars",
                    "vehicles/motorcycles",
                    "vehicles/parts",

                    // real estate
                    "realestate",
                    "realestate/rent",
                    "realestate/sale",
                    "realestate/land",

                    // jobs
                    "jobs",
                    "jobs/fulltime",
                    "jobs/parttime",
                    "jobs/internship",

                    // services
                    "services",
                    "services/handyman",
                    "services/moving",
                    "services/tutoring",

                    // fashion
                    "fashion",
                    "fashion/womensclothing",
                    "fashion/mensclothing",
                    "fashion/accessories",

                    // sports
                    "sports",
                    "sports/fitness",
                    "sports/cycling",
                    "sports/outdoor",

                    // toys
                    "toys",
                    "toys/building",
                    "toys/dolls",
                    "toys/puzzles",
                    "toys/boardgames",

                    // baby and child
                    "babychild",
                    "babychild/strollers",
                    "babychild/carseats",
                    "babychild/clothing",

                    // pets
                    "pets",
                    "pets/dogs",
                    "pets/cats",
                    "pets/supplies",

                    // home and garden
                    "homegarden",
                    "homegarden/furniture",
                    "homegarden/tools",
                    "homegarden/decor",

                    // appliances
                    "appliances",
                    "appliances/kitchen",
                    "appliances/laundry",
                    "appliances/smallappliances",

                    // collectibles
                    "collectibles",
                    "collectibles/coins",
                    "collectibles/stamps",
                    "collectibles/memorabilia",

                    // art
                    "art",
                    "art/paintings",
                    "art/prints",
                    "art/sculpture",

                    // music
                    "music",
                    "music/instruments",
                    "music/vinyl",
                    "music/accessories",

                    // books
                    "books",
                    "books/fiction",
                    "books/nonfiction",
                    "books/textbooks",

                    // games
                    "games",
                    "games/boardgames",
                    "games/videogames",
                    "games/consoles"
            );


            // kleine Helper: legt Parent-Kette an, falls nötig
            for (String raw : paths) {
                ensureCategory(raw, categories);
            }
        };
    }

    private CategoryEntity ensureCategory(String rawPath, CategoryRepository categories) {
        String path = rawPath.trim().toLowerCase(Locale.ROOT);
        return categories.findByPath(path).orElseGet(() -> {
            CategoryEntity parent = null;
            int slash = path.lastIndexOf('/');
            if (slash > 0) {
                String parentPath = path.substring(0, slash);
                parent = ensureCategory(parentPath, categories); // rekursiv
            }
            CategoryEntity created = new CategoryEntity(path, parent);
            return categories.save(created);
        });
    }

    // ---------- 2) Listings ----------

    @Bean
    @Order(2)
    ApplicationRunner seedListings(ListingRepository listings, CategoryRepository categories) {
        return args -> {
            if (listings.count() > 0) return;

            List<ListingEntity> batch = new ArrayList<>();

            // Hilfsfunktion: holt Kategorie per Path
            java.util.function.Function<String, CategoryEntity> cat = p ->
                    categories.findByPath(p).orElseThrow(() -> new IllegalStateException("Missing category: " + p));

            ListingEntity l1 = new ListingEntity();
            l1.setTitle("Samsung Galaxy S21 128GB used");
            l1.setPriceUsd(250L);
            l1.setStatus(ListingStatus.ACTIVE);
            l1.setSellerId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            l1.setImages(List.of("https://picsum.photos/id/100/600/400"));
            l1.setCategory(cat.apply("electronics/phones"));
            batch.add(l1);

            ListingEntity l2 = new ListingEntity();
            l2.setTitle("LG OLED55 C1 55 inch");
            l2.setPriceUsd(700L);
            l2.setStatus(ListingStatus.SOLD);
            l2.setSellerId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            l2.setImages(List.of("https://picsum.photos/id/101/600/400"));
            l2.setCategory(cat.apply("electronics/tv"));
            batch.add(l2);

            ListingEntity l3 = new ListingEntity();
            l3.setTitle("Sony WH1000XM4 headphones");
            l3.setPriceUsd(180L);
            l3.setStatus(ListingStatus.ACTIVE);
            l3.setSellerId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
            l3.setImages(List.of("https://picsum.photos/id/102/600/400"));
            l3.setCategory(cat.apply("electronics/audio"));
            batch.add(l3);

            ListingEntity l4 = new ListingEntity();
            l4.setTitle("Dell XPS 13 16GB 512GB");
            l4.setPriceUsd(850L);
            l4.setStatus(ListingStatus.ACTIVE);
            l4.setSellerId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
            l4.setImages(List.of("https://picsum.photos/id/103/600/400"));
            l4.setCategory(cat.apply("computers/laptops"));
            batch.add(l4);

            ListingEntity l5 = new ListingEntity();
            l5.setTitle("Gaming PC Ryzen 5 GTX 1660");
            l5.setPriceUsd(650L);
            l5.setStatus(ListingStatus.ARCHIVED);
            l5.setSellerId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
            l5.setImages(List.of("https://picsum.photos/id/104/600/400"));
            l5.setCategory(cat.apply("computers/desktops"));
            batch.add(l5);

            ListingEntity l6 = new ListingEntity();
            l6.setTitle("NVIDIA RTX 3080 10GB");
            l6.setPriceUsd(450L);
            l6.setStatus(ListingStatus.ACTIVE);
            l6.setSellerId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
            l6.setImages(List.of("https://picsum.photos/id/105/600/400"));
            l6.setCategory(cat.apply("computers/components"));
            batch.add(l6);

            ListingEntity l7 = new ListingEntity();
            l7.setTitle("Volkswagen Golf 2016 1.6 TDI");
            l7.setPriceUsd(9500L);
            l7.setStatus(ListingStatus.ACTIVE);
            l7.setSellerId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
            l7.setImages(List.of("https://picsum.photos/id/106/600/400"));
            l7.setCategory(cat.apply("vehicles/cars"));
            batch.add(l7);

            ListingEntity l8 = new ListingEntity();
            l8.setTitle("Yamaha MT07 2018");
            l8.setPriceUsd(5200L);
            l8.setStatus(ListingStatus.SOLD);
            l8.setSellerId(UUID.fromString("88888888-8888-8888-8888-888888888888"));
            l8.setImages(List.of("https://picsum.photos/id/107/600/400"));
            l8.setCategory(cat.apply("vehicles/motorcycles"));
            batch.add(l8);

            ListingEntity l9 = new ListingEntity();
            l9.setTitle("Winter tires 205 55 R16 set of 4");
            l9.setPriceUsd(180L);
            l9.setStatus(ListingStatus.ACTIVE);
            l9.setSellerId(UUID.fromString("99999999-9999-9999-9999-999999999999"));
            l9.setImages(List.of("https://picsum.photos/id/108/600/400"));
            l9.setCategory(cat.apply("vehicles/parts"));
            batch.add(l9);

            ListingEntity l10 = new ListingEntity();
            l10.setTitle("Apartment 2 rooms 55sqm city center");
            l10.setPriceUsd(800L);
            l10.setStatus(ListingStatus.ACTIVE);
            l10.setSellerId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            l10.setImages(List.of("https://picsum.photos/id/109/600/400"));
            l10.setCategory(cat.apply("realestate/rent"));
            batch.add(l10);

            ListingEntity l11 = new ListingEntity();
            l11.setTitle("House 4 bedrooms with garden");
            l11.setPriceUsd(350000L);
            l11.setStatus(ListingStatus.ARCHIVED);
            l11.setSellerId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
            l11.setImages(List.of("https://picsum.photos/id/110/600/400"));
            l11.setCategory(cat.apply("realestate/sale"));
            batch.add(l11);

            ListingEntity l12 = new ListingEntity();
            l12.setTitle("Building plot 800sqm");
            l12.setPriceUsd(90000L);
            l12.setStatus(ListingStatus.ACTIVE);
            l12.setSellerId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
            l12.setImages(List.of("https://picsum.photos/id/111/600/400"));
            l12.setCategory(cat.apply("realestate/land"));
            batch.add(l12);

            ListingEntity l13 = new ListingEntity();
            l13.setTitle("Handyman service hourly");
            l13.setPriceUsd(25L);
            l13.setStatus(ListingStatus.ACTIVE);
            l13.setSellerId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
            l13.setImages(List.of("https://picsum.photos/id/112/600/400"));
            l13.setCategory(cat.apply("services/handyman"));
            batch.add(l13);

            ListingEntity l14 = new ListingEntity();
            l14.setTitle("Summer dress floral size M");
            l14.setPriceUsd(18L);
            l14.setStatus(ListingStatus.SOLD);
            l14.setSellerId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
            l14.setImages(List.of("https://picsum.photos/id/113/600/400"));
            l14.setCategory(cat.apply("fashion/womensclothing"));
            batch.add(l14);

            ListingEntity l15 = new ListingEntity();
            l15.setTitle("Road bike aluminum size M");
            l15.setPriceUsd(450L);
            l15.setStatus(ListingStatus.ACTIVE);
            l15.setSellerId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
            l15.setImages(List.of("https://picsum.photos/id/114/600/400"));
            l15.setCategory(cat.apply("sports/cycling"));
            batch.add(l15);

            ListingEntity l16 = new ListingEntity();
            l16.setTitle("Catan base game complete");
            l16.setPriceUsd(30L);
            l16.setStatus(ListingStatus.ACTIVE);
            l16.setSellerId(UUID.fromString("12121212-1212-1212-1212-121212121212"));
            l16.setImages(List.of("https://picsum.photos/id/115/600/400"));
            l16.setCategory(cat.apply("toys/boardgames"));
            batch.add(l16);

            ListingEntity l17 = new ListingEntity();
            l17.setTitle("Stroller Bugaboo Cameleon");
            l17.setPriceUsd(250L);
            l17.setStatus(ListingStatus.ARCHIVED);
            l17.setSellerId(UUID.fromString("13131313-1313-1313-1313-131313131313"));
            l17.setImages(List.of("https://picsum.photos/id/116/600/400"));
            l17.setCategory(cat.apply("babychild/strollers"));
            batch.add(l17);

            ListingEntity l18 = new ListingEntity();
            l18.setTitle("Dog crate medium");
            l18.setPriceUsd(40L);
            l18.setStatus(ListingStatus.ACTIVE);
            l18.setSellerId(UUID.fromString("14141414-1414-1414-1414-141414141414"));
            l18.setImages(List.of("https://picsum.photos/id/117/600/400"));
            l18.setCategory(cat.apply("pets/dogs"));
            batch.add(l18);

            ListingEntity l19 = new ListingEntity();
            l19.setTitle("Dining table solid wood");
            l19.setPriceUsd(350L);
            l19.setStatus(ListingStatus.SOLD);
            l19.setSellerId(UUID.fromString("15151515-1515-1515-1515-151515151515"));
            l19.setImages(List.of("https://picsum.photos/id/118/600/400"));
            l19.setCategory(cat.apply("homegarden/furniture"));
            batch.add(l19);

            ListingEntity l20 = new ListingEntity();
            l20.setTitle("Kitchen stand mixer 1000W");
            l20.setPriceUsd(90L);
            l20.setStatus(ListingStatus.ACTIVE);
            l20.setSellerId(UUID.fromString("16161616-1616-1616-1616-161616161616"));
            l20.setImages(List.of("https://picsum.photos/id/119/600/400"));
            l20.setCategory(cat.apply("appliances/kitchen"));
            batch.add(l20);

            listings.saveAll(batch);
        };
    }
}
