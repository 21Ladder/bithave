package io.github.ladder.backend.listings.service;

import io.github.ladder.backend.listings.domain.ListingStatus;
import io.github.ladder.backend.listings.dto.*;
import io.github.ladder.backend.listings.mapper.ListingMapper;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.listings.persistence.ListingRepository;

import io.github.ladder.backend.listings.persistence.ListingSpecs;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository repo;
    private final ListingMapper mapper;

    public ListingServiceImpl(ListingRepository repo, ListingMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<ListingSummary> list(ListingQuery q) {
        if (q == null) {
            throw new IllegalArgumentException("ListingQuery must not be null");
        }

        // 1. Paging
        final int page = Math.max(0, q.page());
        final int size = q.size() > 0 ? Math.min(q.size(), 100) : 20;

        // 2. Sort (case-insensitive + trim + Whitelist) Fehlerhandling im Falle
        final String requestedSort = q.sort() == null ? "" : q.sort().trim();
        final String sortField = switch (requestedSort) {
            case "priceSats", "pricesats", "PRICESATS" -> "priceSats";
            case "createdAt", "createdat", "CREATEDAT" -> "createdAt";
            default -> "createdAt";
        };

        final String order = q.order() == null ? "" : q.order().trim().toUpperCase(Locale.ROOT);

        // 3. Sort.Direction type wird von String Data direkt interpretiert, in welcher Richtung die Daten sortiert werden sollen
        final Sort.Direction dir = "ASC".equals(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

        // 4. Optionaler Status (mit trim + freundlicher Fehler)
        ListingStatus parsedStatus = null;
        final String statusStr = q.status() == null ? "" : q.status().trim();
        if (!statusStr.isEmpty()) {
            try {
                parsedStatus = ListingStatus.valueOf(statusStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Unsupported status: " + statusStr + " (use ACTIVE|SOLD|ARCHIVED)"
                );
            }
        }

        // 5. erstellt Specification für die WHERE Abfrage durch Spring JPA
        Specification<ListingEntity> spec = null;
        spec = and(spec, ListingSpecs.titleContains(q.q()));
        spec = and(spec, ListingSpecs.statusEquals(parsedStatus));
        spec = and(spec, ListingSpecs.priceMin(q.minPriceSats()));
        spec = and(spec, ListingSpecs.priceMax(q.maxPriceSats()));
        spec = and(spec, ListingSpecs.sellerIdEquals(q.sellerId()));

        // 6. holt mir die Page an Entities mit den gewollten specs
        Page<ListingEntity> pageData = repo.findAll(spec, pageable);

        // 7. Mapping
        List<ListingSummary> items = pageData.getContent().stream()
                .map(mapper::toSummary)
                .toList();

        // 8. Pageresponse wird returned
        return new PageResponse<>(
                items,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.hasNext()
        );
    }

    // helper für List Method um die Specs zu kombinieren
    private static <T> Specification<T> and(Specification<T> base, Specification<T> add) {
        if (add == null) return base;
        return (base == null) ? add : base.and(add);
    }

    @Transactional(readOnly = true)
    @Override
    public ListingResponse getById(UUID id) {

        ListingEntity listingFound = repo.findById(id).orElse(null);

        if (listingFound == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id" + id + " not found");
        }else {
            return mapper.toOneResponse(listingFound);
        }
    }

    @Transactional()
    @Override
    public UUID create(ListingCreateRequest req) {
        ListingEntity entity = mapper.requestToEntity(req);
        repo.save(entity);
        return entity.getId();
    }

    @Transactional
    @Override
    public ListingResponse update(UUID id, ListingUpdateRequest req) {

        //System.out.println("DEBUG incoming price: " + req.priceSats); // oder req.priceSats
        //sucht listingentity in der datenbank
        ListingEntity listingEntity = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));


        ListingStatus currentStatus = listingEntity.getStatus();
        ListingStatus requestStatus = req.status;

        //prüft ob Statuswechsel möglich, wenn nicht wirft HTTPStatus conflict
        if (requestStatus != null && requestStatus != currentStatus) {
            assertStatusTransition(currentStatus, requestStatus);
        }
        //System.out.println("DEBUG price before apply: " + listingEntity.getPriceSats());
        //applied update auf die gefundene ListingEntity
        mapper.applyUpdate(req, listingEntity);

        //System.out.println("DEBUG price before save: " + listingEntity.getPriceSats());
        //speichert die Instanz entgültig ab
        repo.save(listingEntity);

        //System.out.println("DEBUG price after apply: " + listingEntity.getPriceSats());
        //gibt die Daten der neuen Entität zurück
        return mapper.toOneResponse(listingEntity);
    }

    //method um Statuskonflikte zu erkennen deklarieren, aggieren als Guard bei ListingService.update
    private static void assertStatusTransition(ListingStatus from, ListingStatus to) {
        switch (from) {
            case ACTIVE -> {
                if (to != ListingStatus.SOLD && to != ListingStatus.ARCHIVED) {
                    conflict(from, to);
                }
            }
            case SOLD -> {
                if (to != ListingStatus.ARCHIVED) {
                    conflict(from, to);
                }
            }
            case ARCHIVED -> conflict(from, to);
        }
    }

    private static void conflict(ListingStatus from, ListingStatus to) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Status transition not allowed: " + from + " → " + to
        );
    }


    @Override
    public void archive(UUID id) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
