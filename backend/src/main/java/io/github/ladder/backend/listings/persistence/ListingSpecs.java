package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;
import java.util.UUID;

public final class ListingSpecs {

    private ListingSpecs() {}

    public static Specification<ListingEntity> titleContains(String q){
        if (q == null || q.isBlank()){
            return null;
        }

        //Suchbegriff standardisieren
        String searchTerm = "%" + q.toLowerCase(Locale.ROOT) + "%";

        //baut mir die Abfrage für den Searchterm
        return ((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm));
    }

    public static Specification<ListingEntity> statusEquals(ListingStatus status){
        if (status == null){
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<ListingEntity> priceMin(Long min){
        if (min == null){
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("priceSats"), min);
    }

    public static Specification<ListingEntity> priceMax(Long max){
        if (max == null){
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("priceSats"), max);
    }

    public static Specification<ListingEntity> sellerIdEquals(UUID sellerId){
        if (sellerId == null){
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sellerId"), sellerId);
    }

    public static Specification<ListingEntity> categoryPathStartingWithIgnoreCase(String categoryPath){
        if (categoryPath == null || categoryPath.isBlank()) return null;
        String prefix = categoryPath.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("category").get("path")), // <-- wichtig: join + path
                prefix
        );
    }
}
