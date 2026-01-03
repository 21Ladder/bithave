package io.github.ladder.backend.listings.dto

data class CategoryItem(
    val path: String,
    val name: String,
    val hasChildren: Boolean
)
