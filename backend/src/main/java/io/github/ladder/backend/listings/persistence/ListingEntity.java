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

    @Column(name = "price_usd", nullable = false)
    private long priceUsd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "integer default 0")
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false, columnDefinition = "integer default 0")
    private int reservedQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now(); // bei Updates automatisch setzen
    }

    protected ListingEntity() {}

    public ListingEntity(String title, long priceUsd, int quantity, CategoryEntity category, List<String> images, UUID sellerId) {
        this.title = title;
        this.priceUsd = priceUsd;
        this.quantity = Math.max(0, quantity);
        this.category = category;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.sellerId = sellerId;
        this.status = ListingStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.reservedQuantity = 0;
    }

    // --- Getter/Setter (für JPA & Mapper) ---
    public UUID getId() { return id; }

    public UUID getSellerId() { return sellerId; }
    public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getPriceUsd() { return priceUsd; }
    public void setPriceUsd(long priceUsd) { this.priceUsd = priceUsd; }

    public String getCategoryPath() { return this.category.getPath(); }
    public void setCategoryPath(CategoryEntity category) { this.category = category; }

    public CategoryEntity getCategory() { return category; }
    public void setCategory(CategoryEntity category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (quantity < this.reservedQuantity) {
            throw new IllegalArgumentException("Quantity cannot be less than reserved quantity");
        }
        this.quantity = quantity;
    }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) {
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reservedQuantity > this.quantity) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed quantity");
        }
        this.reservedQuantity = reservedQuantity;
    }

    public int getAvailableQuantity() {
        return Math.max(0, this.quantity - this.reservedQuantity);
    }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public void increaseReserved(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be positive");
        setReservedQuantity(this.reservedQuantity + amount);
    }

    public void decreaseReserved(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be positive");
        if (amount > this.reservedQuantity) {
            throw new IllegalArgumentException("Cannot release more than currently reserved");
        }
        setReservedQuantity(this.reservedQuantity - amount);
    }
}
