package io.github.ladder.backend.listings.api;

import io.github.ladder.backend.listings.dto.ListingQuery;
import io.github.ladder.backend.listings.dto.ListingResponse;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.PageResponse;
import io.github.ladder.backend.listings.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ListingsController {

    private final ListingService listingService;

    public ListingsController(ListingService listingService) {
        this.listingService = listingService;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @GetMapping("/listings")
    public PageResponse<ListingSummary> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "order", defaultValue = "DESC") String order,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "minPriceSats", required = false) Long minPriceSats,
            @RequestParam(name = "maxPriceSats", required = false) Long maxPriceSats,
            @RequestParam(name = "sellerId", required = false) UUID sellerId
    ) {

        //nachfolgend Reinigung der Requestparameter bist Zeile 56
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);

        String safeQ        = ListingControllerUtils.normalizeString(q);
        String safeStatus   = ListingControllerUtils.normalizeString(status);
        String safeCategory = ListingControllerUtils.normalizeString(category);
        Long safeMin        = ListingControllerUtils.nonNegativeOrNull(minPriceSats);
        Long safeMax        = ListingControllerUtils.nonNegativeOrNull(maxPriceSats);

        if (safeMin != null && safeMax != null && safeMin > safeMax) {
            throw new IllegalArgumentException("minPriceSats must be <= maxPriceSats");
        }

        ListingQuery query = new ListingQuery(
                safePage, safeSize, sort, order,
                safeQ, safeStatus, safeCategory,
                safeMin, safeMax, sellerId
        );
        return listingService.list(query);
    }

    @GetMapping("listings/{id}")
    public ListingResponse get(@PathVariable UUID id) {
        return listingService.getById(id);
    }

}
