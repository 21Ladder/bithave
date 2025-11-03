package io.github.ladder.backend.orders.api;

import io.github.ladder.backend.orders.dto.CartItemAddRequest;
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest;
import io.github.ladder.backend.orders.dto.CartResponse;
import io.github.ladder.backend.orders.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public CartResponse createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable UUID cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/{cartId}/items")
    public CartResponse addItemToCart(
            @PathVariable UUID cartId,
            @RequestBody @Valid CartItemAddRequest request
    ) {
        return cartService.addItem(cartId, request);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public CartResponse updateCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @RequestBody @Valid CartItemUpdateRequest request
    ) {
        return cartService.updateItem(cartId, itemId, request);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public CartResponse removeCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId
    ) {
        return cartService.removeItem(cartId, itemId);
    }
}
