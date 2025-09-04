// backend/src/main/java/io/github/ladder/backend/listings/persistence/CategoryEntity.java
package io.github.ladder.backend.listings.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "path", nullable = false, length = 120)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")   // <-- parent_id Spalte in DB
    private CategoryEntity parent;    // null = Root

    protected CategoryEntity() {}

    public CategoryEntity(String path, CategoryEntity parent) {
        this.path = path;
        this.parent = parent;           // <-- WICHTIG: setzen!
    }

    public UUID getId() { return id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public CategoryEntity getParent() { return parent; }
    public void setParent(CategoryEntity parent) { this.parent = parent; }
}
