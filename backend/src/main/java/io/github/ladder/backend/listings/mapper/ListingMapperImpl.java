package io.github.ladder.backend.listings.mapper;

import io.github.ladder.backend.listings.dto.ListingCreateRequest;
import io.github.ladder.backend.listings.dto.ListingResponse;
import io.github.ladder.backend.listings.dto.ListingSummary;
import io.github.ladder.backend.listings.dto.ListingUpdateRequest;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListingMapperImpl implements ListingMapper {

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
    public ListingEntity toEntity(ListingCreateRequest req) {
        throw new UnsupportedOperationException("ListingMapper.toEntity not implemented yet");
    }

    @Override
    public void applyUpdate(ListingUpdateRequest req, ListingEntity target) {
        throw new UnsupportedOperationException("ListingMapper.applyUpdate not implemented yet");
    }

    @Override
    public ListingResponse toOneResponse(ListingEntity entity) {

        List<String> safeImages;

        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            safeImages = entity.getImages();
        }else{
            safeImages = null;
        }

        return new ListingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getPriceSats(),
                entity.getStatus(),
                safeImages,
                entity.getSellerId(),
                entity.getCreatedAt(),
                entity.getCreatedAt()
        );
    }
}
