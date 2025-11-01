// io.github.ladder.backend.categories.CategoriesController
package io.github.ladder.backend.listings.api;

import io.github.ladder.backend.listings.dto.CategoryItem;
import io.github.ladder.backend.listings.persistence.CategoryEntity;
import io.github.ladder.backend.listings.persistence.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//gets me the all the categories from the database
@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categories) {
        this.categoryRepository = categories;
    }

    @GetMapping("/categories")
    public List<CategoryItem> list(@RequestParam(required = false) String parent) {
        // 1) Root paths, if no parent param is there, used for the first list of cats
        if (parent == null || parent.trim().isEmpty()) {
            List<CategoryEntity> roots = categoryRepository.findByParentIsNullOrderByPathAsc();
            List<CategoryItem> result = new ArrayList<>();
            for (CategoryEntity e : roots) {
                result.add(toItem(e));
            }
            return result;
        }

        // 2) Children paths, if they exist
        String p = parent.trim().toLowerCase(Locale.ROOT);
        CategoryEntity parentEntity = categoryRepository.findByPath(p)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown category: " + parent));

        List<CategoryEntity> children = categoryRepository.findByParentOrderByPathAsc(parentEntity);
        List<CategoryItem> result = new ArrayList<>();
        for (CategoryEntity e : children) {
            result.add(toItem(e));
        }
        return result;
    }

    // transfrom into a CategoryItem Dto
    private CategoryItem toItem(CategoryEntity e) {
        boolean hasChildren = categoryRepository.existsByParent(e);
        String name = humanizeLastSegment(e.getPath());
        return new CategoryItem(e.getPath(), name, hasChildren);
    }

    //helper to get a nice Name from the path, which I can use in the FE
    private String humanizeLastSegment(String path) {
        int idx = path.lastIndexOf('/');
        String seg = (idx >= 0) ? path.substring(idx + 1) : path; // letztes Segment

        // "feature-phones" -> "Feature Phones"
        String[] parts = seg.split("-");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            String w = parts[i].substring(0, 1).toUpperCase(Locale.ROOT)
                    + (parts[i].length() > 1 ? parts[i].substring(1) : "");
            if (i > 0) b.append(' ');
            b.append(w);
        }
        return b.toString();
    }
}
