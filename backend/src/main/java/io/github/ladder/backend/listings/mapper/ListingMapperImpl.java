package io.github.ladder.backend.listings.mapper;

import io.github.ladder.backend.listings.dto.ListingCreateRequest;
import io.github.ladder.backend.listings.dto.ListingResponse;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.ListingUpdateRequest;
import io.github.ladder.backend.listings.persistence.CategoryEntity;
import io.github.ladder.backend.listings.persistence.CategoryRepository;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.listings.persistence.ListingRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingMapperImpl implements ListingMapper {

    private final CategoryRepository categoryRepository;

    public ListingMapperImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ListingSummary toSummary(ListingEntity e) {

        String thumbnail;

        if (e.getImages() != null && !e.getImages().isEmpty()) {
            thumbnail = e.getImages().get(0);
        } else {
            thumbnail = null;
        }

        return new ListingSummary(
                e.getId(),
                e.getTitle(),
                e.getPriceUsd(),
                e.getCategoryPath(),
                e.getStatus(),
                thumbnail,
                e.getCreatedAt()
        );
    }

    @Override
    public ListingEntity requestToEntity(ListingCreateRequest req) {

        List<String> allImages = new ArrayList<>();

        if (req.images != null) {
            allImages.addAll(req.images);
        }

        String cp = req.categoryPath == null ? null : req.categoryPath.trim().toLowerCase();
        if (cp == null || cp.isEmpty()) {
            throw new IllegalArgumentException("categoryPath required");
        }
        CategoryEntity category = categoryRepository.findByPath(cp)
                .orElseThrow(() -> new IllegalArgumentException("Category path not found: " + req.categoryPath));

        return new ListingEntity(
                req.title,
                req.priceUsd,
                category,
                allImages,
                req.sellerId
        );
    }

    @Override
    public void applyUpdate(ListingUpdateRequest req, ListingEntity target) {

        if (req.title != null) {
            String t = req.title.trim();
            if (req.title.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            } else {
                target.setTitle(t);
            }
        }

        //System.out.println("DEBUG in the mapperimpl price before save: " + req.priceSats);
        if (req.priceUsd != null) {
            if (req.priceUsd < 0) {
                throw new IllegalArgumentException("PriceSats cannot be negative");
            }
            target.setPriceUsd(req.priceUsd);
        }

        if (req.status != null) {
            target.setStatus(req.status);
        }

        if (req.images != null) {
            target.setImages(new ArrayList<>(req.images));
        }

        if (req.categoryPath != null) {
            String cp = req.categoryPath.trim().toLowerCase();
            CategoryEntity category = categoryRepository.findByPath(cp)
                    .orElseThrow(() -> new IllegalArgumentException("Category path not found: " + req.categoryPath));
            target.setCategory(category);
        }

    }

    @Override
    public ListingResponse toOneResponse(ListingEntity entity) {

        List<String> safeImages;

        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            safeImages = entity.getImages();
        }else{
            safeImages = new ArrayList<>();
        }

        return new ListingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPriceUsd(),
                entity.getCategoryPath(),
                entity.getStatus(),
                safeImages,
                entity.getSellerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
