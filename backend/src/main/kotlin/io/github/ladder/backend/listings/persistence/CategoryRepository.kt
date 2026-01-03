package io.github.ladder.backend.listings.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.Optional
import java.util.UUID

interface CategoryRepository : JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {
    fun findByPath(path: String): Optional<CategoryEntity>
    fun findByParentIsNullOrderByPathAsc(): List<CategoryEntity>
    fun findByParentOrderByPathAsc(parent: CategoryEntity): List<CategoryEntity>
    fun existsByParent(parent: CategoryEntity): Boolean
}
