package io.github.ladder.backend.listings.mapper;

import io.github.ladder.backend.listings.dto.ListingCreateRequest;
import io.github.ladder.backend.listings.dto.ListingResponse;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.ListingUpdateRequest;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.listings.persistence.ListingRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingMapperImpl implements ListingMapper {

    public ListingMapperImpl(ListingRepository listingRepository) {
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
                e.getPriceSats(),
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

        ListingEntity newListingEntity = new ListingEntity(
                req.title,
                req.priceSats,
                allImages,
                req.sellerId
        );

        return newListingEntity;
    }

    @Override
    public void applyUpdate(ListingUpdateRequest req, ListingEntity target) {

        if (req.title != null) {
            req.title.trim();
            if (req.title.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            } else {
                target.setTitle(req.title);
            }
        }

        //System.out.println("DEBUG in the mapperimpl price before save: " + req.priceSats);
        if (req.priceSats != null) {
            if (req.priceSats < 0) {
                throw new IllegalArgumentException("PriceSats cannot be negative");
            }
            target.setPriceSats(req.priceSats);
        }

        if (req.status != null) {
            target.setStatus(req.status);
        }

        if (req.images != null && req.images.isEmpty()) {
            target.setImages(new ArrayList<>(req.images));
        }else{
            target.setImages(req.images);
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
                entity.getPriceSats(),
                entity.getStatus(),
                safeImages,
                entity.getSellerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
