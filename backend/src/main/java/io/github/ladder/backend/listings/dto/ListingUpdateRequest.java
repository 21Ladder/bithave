package io.github.ladder.backend.listings.dto;

import java.awt.image.BufferedImage;

public class ListingUpdateRequest {
    private String title;
    private String description;
    private Integer priceSats;
    private BufferedImage[] images;
    private String category;
}
