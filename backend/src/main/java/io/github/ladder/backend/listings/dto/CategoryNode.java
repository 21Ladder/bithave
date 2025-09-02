package io.github.ladder.backend.listings.dto;

import java.util.List;
public record CategoryNode(
        String code,
        String label,
        List<CategoryNode> children
) {}
