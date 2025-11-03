package io.github.ladder.backend.orders.mapper;

import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.orders.dto.CartItemResponse;
import io.github.ladder.backend.orders.dto.CartResponse;
import io.github.ladder.backend.orders.persistence.CartEntity;
import io.github.ladder.backend.orders.persistence.CartItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(CartEntity cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        int totalQuantity = items.stream().mapToInt(CartItemResponse::quantity).sum();
        long totalPriceUsd = items.stream().mapToLong(CartItemResponse::subtotalUsd).sum();

        return new CartResponse(
                cart.getId(),
                items,
                totalQuantity,
                totalPriceUsd
        );
    }

    private CartItemResponse toItemResponse(CartItemEntity item) {
        ListingEntity listing = item.getListing();
        long subtotal = item.getUnitPriceUsd() * item.getQuantity();
        String thumbnail = listing.getImages().isEmpty() ? null : listing.getImages().get(0);

        return new CartItemResponse(
                item.getId(),
                listing.getId(),
                listing.getTitle(),
                thumbnail,
                item.getUnitPriceUsd(),
                item.getQuantity(),
                subtotal
        );
    }
}
