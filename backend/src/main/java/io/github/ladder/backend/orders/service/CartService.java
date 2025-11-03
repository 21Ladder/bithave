package io.github.ladder.backend.orders.service;

import io.github.ladder.backend.orders.dto.CartItemAddRequest;
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest;
import io.github.ladder.backend.orders.dto.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse createCart();

    CartResponse getCart(UUID cartId);

    CartResponse addItem(UUID cartId, CartItemAddRequest request);

    CartResponse updateItem(UUID cartId, UUID itemId, CartItemUpdateRequest request);

    CartResponse removeItem(UUID cartId, UUID itemId);
}
