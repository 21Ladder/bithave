package io.github.ladder.backend.listings.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

    Optional<CategoryEntity> findByPath(String path);
    List<CategoryEntity> findByParentIsNullOrderByPathAsc();
    List<CategoryEntity> findByParentOrderByPathAsc(CategoryEntity parent);
    boolean existsByParent(CategoryEntity parent);
}
