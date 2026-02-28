package io.github.ladder.backend.listings.persistence

import io.github.ladder.backend.listings.domain.ListingStatus
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import java.util.Locale
import java.util.UUID

@Profile("dev", "docker")
@Configuration
class ListingsDevSeeder {

    @Bean
    @Order(1)
    fun seedCategories(categories: CategoryRepository) = ApplicationRunner {
        if (categories.count() > 0) return@ApplicationRunner

        val paths = listOf(
            "aquariums-and-cabinets",
            "aquariums-and-cabinets/float-glass",
            "aquariums-and-cabinets/clear-glass",
            "aquariums-and-cabinets/aquarium-sets",
            "aquariums-and-cabinets/cabinets",
            "aquariums-and-cabinets/aquarium-accessories",
            "aquariums-and-cabinets/wabikusa",
            "lighting",
            "lighting/led-systems",
            "lighting/lamps",
            "lighting/power-supplies-controllers",
            "lighting/mounts-hanging-kits",
            "lighting/spare-parts",
            "substrate-and-hardscape",
            "substrate-and-hardscape/substrate",
            "substrate-and-hardscape/sand-gravel",
            "substrate-and-hardscape/stones",
            "substrate-and-hardscape/driftwood",
            "substrate-and-hardscape/hardscape-accessories",
            "co2",
            "co2/cylinders",
            "co2/regulators",
            "co2/diffusers",
            "co2/drop-checkers",
            "co2/co2-sets",
            "co2/co2-accessories",
            "filtration",
            "filtration/external-filters",
            "filtration/internal-filters",
            "filtration/hang-on-filters",
            "filtration/inflow-outflow",
            "filtration/filter-media",
            "filtration/pumps",
            "filtration/skimmers",
            "filtration/filtration-accessories",
            "plants",
            "plants/in-vitro",
            "plants/carpeting",
            "plants/mosses",
            "plants/foreground",
            "plants/midground",
            "plants/background",
            "plants/floating-plants",
            "plants/epiphytes",
            "plants/plant-accessories",
            "ferts-and-care",
            "ferts-and-care/fertilizers",
            "ferts-and-care/water-tests",
            "ferts-and-care/conditioners",
            "ferts-and-care/algae-control",
            "ferts-and-care/bacteria-additives",
            "ferts-and-care/dosing-aids",
            "livestock-care",
            "livestock-care/food",
            "livestock-care/treatments-meds",
            "livestock-care/care-accessories",
            "livestock-care/minerals",
            "livestock-care/water-additives",
            "tools",
            "tools/tweezers",
            "tools/scissors",
            "tools/sand-flatteners",
            "tools/algae-tools",
            "tools/care-tools",
            "tools/dosing-tools",
            "tools/tool-holders",
            "tools/tool-accessories",
            "books-and-prints",
            "books-and-prints/books-brochures",
            "books-and-prints/posters-artworks",
            "books-and-prints/merchandise",
            "books-and-prints/gift-cards",
            "livestock",
            "livestock/fish",
            "livestock/shrimp",
            "livestock/snails-and-inverts"
        )

        paths.forEach { raw -> ensureCategory(raw, categories) }
    }

    private fun ensureCategory(rawPath: String, categories: CategoryRepository): CategoryEntity {
        val path = rawPath.trim().lowercase(Locale.ROOT)
        return categories.findByPath(path).orElseGet {
            val parent = path.lastIndexOf('/').takeIf { it > 0 }?.let { slash ->
                val parentPath = path.substring(0, slash)
                ensureCategory(parentPath, categories)
            }
            categories.save(CategoryEntity(path, parent))
        }
    }

    @Bean
    @Order(2)
    fun seedListings(listings: ListingRepository, categories: CategoryRepository) = ApplicationRunner {
        if (listings.count() > 0) return@ApplicationRunner

        val cat: (String) -> CategoryEntity = { p ->
            categories.findByPath(p).orElseThrow { IllegalStateException("Missing category: $p") }
        }

        val batch = listOf(
            ListingEntity().apply {
                title = "Clear glass aquarium 60P set"
                priceUsd = 420L
                quantity = 3
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("11111111-1111-1111-1111-111111111111")
                images = listOf("/listingPictures/60p.jpg")
                category = cat("aquariums-and-cabinets/clear-glass")
            },
            ListingEntity().apply {
                title = "Float glass nano cube 30L"
                priceUsd = 95L
                quantity = 8
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("22222222-2222-2222-2222-222222222222")
                images = listOf("/listingPictures/floatcube.webp")
                category = cat("aquariums-and-cabinets/float-glass")
            },
            ListingEntity().apply {
                title = "Plug-and-play 90P aquarium set"
                priceUsd = 690L
                quantity = 2
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                images = listOf("/listingPictures/90p-set.jpg")
                category = cat("aquariums-and-cabinets/aquarium-sets")
            },
            ListingEntity().apply {
                title = "Oak cabinet 90 cm"
                priceUsd = 230L
                quantity = 5
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                images = listOf("/listingPictures/oakcabinet.jpeg")
                category = cat("aquariums-and-cabinets/cabinets")
            },
            ListingEntity().apply {
                title = "Aquarium leveling mat 60P"
                priceUsd = 12L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
                images = listOf("/listingPictures/leveling-mat-60p.jpeg")
                category = cat("aquariums-and-cabinets/aquarium-accessories")
            },
            ListingEntity().apply {
                title = "Wabikusa bowl starter set"
                priceUsd = 58L
                quantity = 10
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
                images = listOf("/listingPictures/wabikus-starterset.jpg")
                category = cat("aquariums-and-cabinets/wabikusa")
            },
            ListingEntity().apply {
                title = "LED Matrix 60 cm plant light"
                priceUsd = 210L
                quantity = 5
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("33333333-3333-3333-3333-333333333333")
                images = listOf("/listingPictures/led-matrix-60.jpg")
                category = cat("lighting/led-systems")
            },
            ListingEntity().apply {
                title = "Clip-on nano lamp"
                priceUsd = 45L
                quantity = 18
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
                images = listOf("/listingPictures/clipon-nano-lamp.avif")
                category = cat("lighting/lamps")
            },
            ListingEntity().apply {
                title = "LED dimmer controller"
                priceUsd = 29L
                quantity = 22
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
                images = listOf("/listingPictures/led-dimmer-controller.png")
                category = cat("lighting/power-supplies-controllers")
            },
            ListingEntity().apply {
                title = "Stainless steel hanging kit"
                priceUsd = 48L
                quantity = 14
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("44444444-4444-4444-4444-444444444444")
                images = listOf("/listingPictures/stainless-steel-hanging-kit.webp")
                category = cat("lighting/mounts-hanging-kits")
            },
            ListingEntity().apply {
                title = "Replacement LED module 30 cm"
                priceUsd = 35L
                quantity = 12
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("12121212-1212-1212-1212-121212121212")
                images = listOf("/listingPictures/replacement-led-module.jpg")
                category = cat("lighting/spare-parts")
            },
            ListingEntity().apply {
                title = "Amazonia soil 9L"
                priceUsd = 39L
                quantity = 25
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("55555555-5555-5555-5555-555555555555")
                images = listOf("/listingPictures/amazonia-soil-9l.jpg")
                category = cat("substrate-and-hardscape/substrate")
            },
            ListingEntity().apply {
                title = "River sand 5 kg"
                priceUsd = 18L
                quantity = 30
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("14141414-1414-1414-1414-141414141414")
                images = listOf("/listingPictures/river-sand-5kg.png")
                category = cat("substrate-and-hardscape/sand-gravel")
            },
            ListingEntity().apply {
                title = "Seiryu stone bundle 10 kg"
                priceUsd = 75L
                quantity = 6
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("66666666-6666-6666-6666-666666666666")
                images = listOf("/listingPictures/seiryu-stone-bundle-10kg.jpg")
                category = cat("substrate-and-hardscape/stones")
            },
            ListingEntity().apply {
                title = "Spider wood branch large"
                priceUsd = 32L
                quantity = 15
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("15151515-1515-1515-1515-151515151515")
                images = listOf("/listingPictures/spider-wood-branch-large.jpeg")
                category = cat("substrate-and-hardscape/driftwood")
            },
            ListingEntity().apply {
                title = "Hardscape glue gel"
                priceUsd = 9L
                quantity = 60
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("16161616-1616-1616-1616-161616161616")
                images = listOf("/listingPictures/hardscape-glue-gel.jpg")
                category = cat("substrate-and-hardscape/hardscape-accessories")
            },
            ListingEntity().apply {
                title = "CO2 refillable cylinder 2 kg"
                priceUsd = 95L
                quantity = 12
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("17171717-1717-1717-1717-171717171717")
                images = listOf("/listingPictures/co2-refillable-cylinder-kit.jpg")
                category = cat("co2/cylinders")
            },
            ListingEntity().apply {
                title = "CO2 dual-stage regulator"
                priceUsd = 165L
                quantity = 9
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("77777777-7777-7777-7777-777777777777")
                images = listOf("/listingPictures/co2-dual-stage-regulator.webp")
                category = cat("co2/regulators")
            },
            ListingEntity().apply {
                title = "CO2 glass diffuser 20 mm"
                priceUsd = 22L
                quantity = 18
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("88888888-8888-8888-8888-888888888888")
                images = listOf("/listingPictures/co2-glass-diffuser-20-mm.jpg")
                category = cat("co2/diffusers")
            },
            ListingEntity().apply {
                title = "Drop checker kit with indicator"
                priceUsd = 19L
                quantity = 30
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("99999999-9999-9999-9999-999999999999")
                images = listOf("/listingPictures/drop-checker-kit-with-indicator.jpeg")
                category = cat("co2/drop-checkers")
            },
            ListingEntity().apply {
                title = "Complete CO2 set for 60L"
                priceUsd = 185L
                quantity = 7
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("18181818-1818-1818-1818-181818181818")
                images = listOf("/listingPictures/complete-co2-set-for-60l.jpg")
                category = cat("co2/co2-sets")
            },
            ListingEntity().apply {
                title = "CO2 resistant tubing black 3m"
                priceUsd = 8L
                quantity = 50
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("19191919-1919-1919-1919-191919191919")
                images = listOf("/listingPictures/co2-resistant-tubing-black-3m.jpeg")
                category = cat("co2/co2-accessories")
            },
            ListingEntity().apply {
                title = "External filter 1200 L/h"
                priceUsd = 180L
                quantity = 4
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("20202020-2020-2020-2020-202020202020")
                images = listOf("/listingPictures/external-filter-1200lh.jpeg")
                category = cat("filtration/external-filters")
            },
            ListingEntity().apply {
                title = "Internal filter 400 L/h"
                priceUsd = 35L
                quantity = 20
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("21212121-2121-2121-2121-212121212121")
                images = listOf("/listingPictures/internal-filter-400-l-h.jpg")
                category = cat("filtration/internal-filters")
            },
            ListingEntity().apply {
                title = "Hang-on filter slim 200"
                priceUsd = 42L
                quantity = 15
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("23232323-2323-2323-2323-232323232323")
                images = listOf("/listingPictures/hang-on-filter-slim-200.jpeg")
                category = cat("filtration/hang-on-filters")
            },
            ListingEntity().apply {
                title = "Glass lily pipe set 16/22"
                priceUsd = 65L
                quantity = 11
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                images = listOf("/listingPictures/glass-lily-pipe-set-16-22.webp")
                category = cat("filtration/inflow-outflow")
            },
            ListingEntity().apply {
                title = "Bio filter media ceramic rings"
                priceUsd = 16L
                quantity = 60
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("24242424-2424-2424-2424-242424242424")
                images = listOf("/listingPictures/bio-filter-media-ceramic-rings.jpg")
                category = cat("filtration/filter-media")
            },
            ListingEntity().apply {
                title = "Circulation pump 2000 L/h"
                priceUsd = 55L
                quantity = 9
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("25252525-2525-2525-2525-252525252525")
                images = listOf("/listingPictures/circulation-pump-2000lh.jpg")
                category = cat("filtration/pumps")
            },
            ListingEntity().apply {
                title = "Surface skimmer glass"
                priceUsd = 28L
                quantity = 22
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("26262626-2626-2626-2626-262626262626")
                images = listOf("/listingPictures/surface-skimmer-glass.jpg")
                category = cat("filtration/skimmers")
            },
            ListingEntity().apply {
                title = "Filter hose 16/22 mm 3m"
                priceUsd = 14L
                quantity = 35
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("27272727-2727-2727-2727-272727272727")
                images = listOf("/listingPictures/filter-hose-16-22-mm-3m.jpg")
                category = cat("filtration/filtration-accessories")
            },
            ListingEntity().apply {
                title = "In-vitro Micranthemum Monte Carlo"
                priceUsd = 12L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("28282828-2828-2828-2828-282828282828")
                images = listOf("/listingPictures/in-vitro-micranthemum-monte-carlo.jpg")
                category = cat("plants/in-vitro")
            },
            ListingEntity().apply {
                title = "In-vitro Hemianthus cuba"
                priceUsd = 14L
                quantity = 30
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("29292929-2929-2929-2929-292929292929")
                images = listOf("/listingPictures/in-vitro-hemianthus-cuba.jpg")
                category = cat("plants/carpeting")
            },
            ListingEntity().apply {
                title = "Java moss portion"
                priceUsd = 10L
                quantity = 50
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("30303030-3030-3030-3030-303030303030")
                images = listOf("/listingPictures/java-moss-portion.jpeg")
                category = cat("plants/mosses")
            },
            ListingEntity().apply {
                title = "Helanthium tenellum pot"
                priceUsd = 8L
                quantity = 45
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("31313131-3131-3131-3131-313131313131")
                images = listOf("/listingPictures/helanthium-tenellum-pot.webp")
                category = cat("plants/foreground")
            },
            ListingEntity().apply {
                title = "Cryptocoryne wendtii green"
                priceUsd = 7L
                quantity = 33
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("32323232-3232-3232-3232-323232323232")
                images = listOf("/listingPictures/cryptocoryne-wendtii-green.jpg")
                category = cat("plants/midground")
            },
            ListingEntity().apply {
                title = "Vallisneria gigantea bunch"
                priceUsd = 9L
                quantity = 28
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("33333333-3333-3333-3333-333333333333")
                images = listOf("/listingPictures/vallisneria-gigantea-bunch.jpg")
                category = cat("plants/background")
            },
            ListingEntity().apply {
                title = "Salvinia natans portion"
                priceUsd = 6L
                quantity = 70
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("34343434-3434-3434-3434-343434343434")
                images = listOf("/listingPictures/salvinia-natans-portion.jpeg")
                category = cat("plants/floating-plants")
            },
            ListingEntity().apply {
                title = "Anubias petite on lava rock"
                priceUsd = 16L
                quantity = 27
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
                images = listOf("/listingPictures/anubias-petite-on-lava-rock.webp")
                category = cat("plants/epiphytes")
            },
            ListingEntity().apply {
                title = "Root fertilizer tabs (10x)"
                priceUsd = 11L
                quantity = 80
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("35353535-3535-3535-3535-353535353535")
                images = listOf("/listingPictures/root-fertilizer-tabs-10x.webp")
                category = cat("plants/plant-accessories")
            },
            ListingEntity().apply {
                title = "Comprehensive liquid fertilizer 500ml"
                priceUsd = 17L
                quantity = 33
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
                images = listOf("/listingPictures/comprehensive-liquid-fertilizer-500ml.jpg")
                category = cat("ferts-and-care/fertilizers")
            },
            ListingEntity().apply {
                title = "Master water test kit"
                priceUsd = 45L
                quantity = 13
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("36363636-3636-3636-3636-363636363636")
                images = listOf("/listingPictures/master-water-test-kit.jpg")
                category = cat("ferts-and-care/water-tests")
            },
            ListingEntity().apply {
                title = "Water conditioner 250ml"
                priceUsd = 12L
                quantity = 55
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("37373737-3737-3737-3737-373737373737")
                images = listOf("/listingPictures/water-conditioner-250ml.jpg")
                category = cat("ferts-and-care/conditioners")
            },
            ListingEntity().apply {
                title = "Algae control liquid carbon"
                priceUsd = 14L
                quantity = 21
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
                images = listOf("/listingPictures/algae-control-liquid-carbon.jpg")
                category = cat("ferts-and-care/algae-control")
            },
            ListingEntity().apply {
                title = "Starter bacteria 100ml"
                priceUsd = 10L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("38383838-3838-3838-3838-383838383838")
                images = listOf("/listingPictures/starter-bacteria-100ml.jpg")
                category = cat("ferts-and-care/bacteria-additives")
            },
            ListingEntity().apply {
                title = "Dosing pump 3-channel"
                priceUsd = 149L
                quantity = 6
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("39393939-3939-3939-3939-393939393939")
                images = listOf("/listingPictures/dosing-pump-3-channel.jpg")
                category = cat("ferts-and-care/dosing-aids")
            },
            ListingEntity().apply {
                title = "Premium shrimp food 50g"
                priceUsd = 9L
                quantity = 60
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("14141414-1414-1414-1414-141414141414")
                images = listOf("/listingPictures/premium-shrimp-food-50g.jpeg")
                category = cat("livestock-care/food")
            },
            ListingEntity().apply {
                title = "Ich treatment 100ml"
                priceUsd = 13L
                quantity = 25
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("40404040-4040-4040-4040-404040404040")
                images = listOf("/listingPictures/ich-treatment-100ml.jpeg")
                category = cat("livestock-care/treatments-meds")
            },
            ListingEntity().apply {
                title = "Fish net 10 cm"
                priceUsd = 6L
                quantity = 70
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("41414141-4141-4141-4141-414141414141")
                images = listOf("/listingPictures/fish-net-10-cm.jpg")
                category = cat("livestock-care/care-accessories")
            },
            ListingEntity().apply {
                title = "Shrimp mineral salt 250g"
                priceUsd = 11L
                quantity = 50
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("13131313-1313-1313-1313-131313131313")
                images = listOf("/listingPictures/shrimp-mineral-salt-250g.webp")
                category = cat("livestock-care/minerals")
            },
            ListingEntity().apply {
                title = "GH+ remineralizer 500g"
                priceUsd = 16L
                quantity = 38
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("42424242-4242-4242-4242-424242424242")
                images = listOf("/listingPictures/gh-remineralizer-500g.jpg")
                category = cat("livestock-care/water-additives")
            },
            ListingEntity().apply {
                title = "Curved aquascaping tweezers 27cm"
                priceUsd = 24L
                quantity = 18
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("15151515-1515-1515-1515-151515151515")
                images = listOf("/listingPictures/curved-aquascaping-tweezers-27cm.jpg")
                category = cat("tools/tweezers")
            },
            ListingEntity().apply {
                title = "Spring scissors 16cm"
                priceUsd = 32L
                quantity = 7
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("16161616-1616-1616-1616-161616161616")
                images = listOf("/listingPictures/spring-scissors-16cm.jpg")
                category = cat("tools/scissors")
            },
            ListingEntity().apply {
                title = "Sand flattener spatula"
                priceUsd = 14L
                quantity = 26
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("43434343-4343-4343-4343-434343434343")
                images = listOf("/listingPictures/sand-flattener-spatula.webp")
                category = cat("tools/sand-flatteners")
            },
            ListingEntity().apply {
                title = "Algae scraper stainless"
                priceUsd = 18L
                quantity = 30
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("44444444-4444-4444-4444-444444444444")
                images = listOf("/listingPictures/algae-scraper-stainless.jpeg")
                category = cat("tools/algae-tools")
            },
            ListingEntity().apply {
                title = "Cleaning sponge on stick"
                priceUsd = 9L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("45454545-4545-4545-4545-454545454545")
                images = listOf("/listingPictures/cleaning-sponge-on-stick.jpg")
                category = cat("tools/care-tools")
            },
            ListingEntity().apply {
                title = "Measuring pipette set"
                priceUsd = 7L
                quantity = 80
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("46464646-4646-4646-4646-464646464646")
                images = listOf("/listingPictures/measuring-pipette-set.jpg")
                category = cat("tools/dosing-tools")
            },
            ListingEntity().apply {
                title = "Magnetic tool holder"
                priceUsd = 22L
                quantity = 16
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("47474747-4747-4747-4747-474747474747")
                images = listOf("/listingPictures/magnetic-tool-holder.webp")
                category = cat("tools/tool-holders")
            },
            ListingEntity().apply {
                title = "Replacement tool tips set"
                priceUsd = 8L
                quantity = 55
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("48484848-4848-4848-4848-484848484848")
                images = listOf("/listingPictures/replacement-tool-tips-set.png")
                category = cat("tools/tool-accessories")
            },
            ListingEntity().apply {
                title = "Aquascaping handbook"
                priceUsd = 28L
                quantity = 5
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("17171717-1717-1717-1717-171717171717")
                images = listOf("/listingPictures/aquascaping-handbook.jpeg")
                category = cat("books-and-prints/books-brochures")
            },
            ListingEntity().apply {
                title = "Aquascape poster A2"
                priceUsd = 15L
                quantity = 25
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("49494949-4949-4949-4949-494949494949")
                images = listOf("/listingPictures/aquascape-poster-a2.webp")
                category = cat("books-and-prints/posters-artworks")
            },
            ListingEntity().apply {
                title = "Logo t-shirt size M"
                priceUsd = 22L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("50505050-5050-5050-5050-505050505050")
                images = listOf("/listingPictures/logo-t-shirt-size-m.webp")
                category = cat("books-and-prints/merchandise")
            },
            ListingEntity().apply {
                title = "Gift card 50 USD"
                priceUsd = 50L
                quantity = 200
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("18181818-1818-1818-1818-181818181818")
                images = listOf("/listingPictures/gift-card-50-usd.webp")
                category = cat("books-and-prints/gift-cards")
            },
            ListingEntity().apply {
                title = "Community tetras pack (10x)"
                priceUsd = 35L
                quantity = 12
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("19191919-1919-1919-1919-191919191919")
                images = listOf("/listingPictures/community-tetras-pack-10x.webp")
                category = cat("livestock/fish")
            },
            ListingEntity().apply {
                title = "Red cherry shrimp (10 pcs)"
                priceUsd = 24L
                quantity = 30
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("20202020-2020-2020-2020-202020202020")
                images = listOf("/listingPictures/red-cherry-shrimp-10-pcs.jpeg")
                category = cat("livestock/shrimp")
            },
            ListingEntity().apply {
                title = "Nerite snail algae crew"
                priceUsd = 12L
                quantity = 40
                status = ListingStatus.ACTIVE
                sellerId = UUID.fromString("21212121-2121-2121-2121-212121212121")
                images = listOf("/listingPictures/nerite-snail-algae-crew.webp")
                category = cat("livestock/snails-and-inverts")
            }
        )

        listings.saveAll(batch)
    }
}
