package io.github.ladder.backend.listings.api

import io.github.ladder.backend.listings.dto.CategoryItem
import io.github.ladder.backend.listings.persistence.CategoryEntity
import io.github.ladder.backend.listings.persistence.CategoryRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.Locale

// REST controller to manage and retrieve listing categories
@RestController
@RequestMapping("/api/v1")
class CategoryController(
    private val categoryRepository: CategoryRepository
) {

    // List categories, optionally filtered by parent category
    @GetMapping("/categories")
    fun list(@RequestParam(required = false) parent: String?): List<CategoryItem> {
        if (parent.isNullOrBlank()) {
            return categoryRepository.findByParentIsNullOrderByPathAsc().map { toItem(it) }
        }

        val p = parent.trim().lowercase(Locale.ROOT)
        val parentEntity = categoryRepository.findByPath(p)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown category: $parent") }

        return categoryRepository.findByParentOrderByPathAsc(parentEntity).map { toItem(it) }
    }

    // Convert CategoryEntity to CategoryItem DTO
    private fun toItem(e: CategoryEntity): CategoryItem {
        val hasChildren = categoryRepository.existsByParent(e)
        val name = humanizeLastSegment(e.path)
        return CategoryItem(e.path, name, hasChildren)
    }

    // Humanize the last segment of a category path for display purposes
    private fun humanizeLastSegment(path: String): String {
        val idx = path.lastIndexOf('/')
        val seg = if (idx >= 0) path.substring(idx + 1) else path
        return seg.split("-")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { c -> c.titlecase(Locale.ROOT) }
            }
    }
}
