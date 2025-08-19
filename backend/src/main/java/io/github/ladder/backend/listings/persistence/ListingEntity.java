package io.github.ladder.backend.listings.persistence;

import io.github.ladder.backend.listings.domain.ListingStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings") // Tabellenname festlegen (stabil für später)
public class ListingEntity {

    @Id
    @GeneratedValue
    @UuidGenerator                 // UUID wird in der App generiert
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "price_sats", nullable = false)
    private long priceSats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    // Bilder als eigene Tabelle 'listing_images'
    @ElementCollection
    @CollectionTable(
            name = "listing_images",
            joinColumns = @JoinColumn(name = "listing_id")
    )
    @OrderColumn(name = "position") // 0,1,2… → stabile Reihenfolge
    @Column(name = "url", nullable = false, columnDefinition = "text")
    private List<String> images = new ArrayList<>();

    // --- Lifecycle-Hooks: Timestamps/Defaults setzen ---
    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = ListingStatus.ACTIVE;
    }

    protected ListingEntity() {} // JPA no-args ctor

    // --- Getter/Setter (für JPA & Mapper) ---
    public UUID getId() { return id; }

    public UUID getSellerId() { return sellerId; }
    public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getPriceSats() { return priceSats; }
    public void setPriceSats(long priceSats) { this.priceSats = priceSats; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
