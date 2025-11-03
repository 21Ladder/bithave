package io.github.ladder.backend.orders.service;

import io.github.ladder.backend.listings.domain.ListingStatus;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.listings.persistence.ListingRepository;
import io.github.ladder.backend.orders.dto.CartItemAddRequest;
import io.github.ladder.backend.orders.dto.CartItemUpdateRequest;
import io.github.ladder.backend.orders.dto.CartResponse;
import io.github.ladder.backend.orders.persistence.CartEntity;
import io.github.ladder.backend.orders.persistence.CartItemEntity;
import io.github.ladder.backend.orders.persistence.CartRepository;
import org.springframework.http.HttpStatus;
import io.github.ladder.backend.orders.mapper.CartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Service
class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ListingRepository listingRepository;
    private final CartMapper cartMapper;

    CartServiceImpl(
            CartRepository cartRepository,
            ListingRepository listingRepository,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.listingRepository = listingRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public CartResponse createCart() {
        CartEntity cart = new CartEntity();
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID cartId) {
        CartEntity cart = loadCart(cartId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID cartId, CartItemAddRequest request) {
        CartEntity cart = loadCart(cartId);

        int quantity = request.quantity() == null ? 1 : request.quantity();
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        ListingEntity listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not available");
        }

        CartItemEntity existing = cart.getItems().stream()
                .filter(item -> Objects.equals(item.getListing().getId(), listing.getId()))
                .findFirst()
                .orElse(null);

        int increment = quantity;
        int available = listing.getAvailableQuantity();
        if (available < increment) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough quantity available");
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUnitPriceUsd(listing.getPriceUsd());
        } else {
            CartItemEntity newItem = new CartItemEntity(listing, quantity, listing.getPriceUsd());
            cart.addItem(newItem);
        }

        listing.increaseReserved(increment);

        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID cartId, UUID itemId, CartItemUpdateRequest request) {
        CartEntity cart = loadCart(cartId);

        if (request.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        CartItemEntity item = cart.getItems().stream()
                .filter(ci -> Objects.equals(ci.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));

        ListingEntity listing = item.getListing();
        int oldQuantity = item.getQuantity();
        int newQuantity = request.quantity();
        int delta = newQuantity - oldQuantity;

        if (delta > 0) {
            int available = listing.getAvailableQuantity();
            if (available < delta) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough quantity available");
            }
            listing.increaseReserved(delta);
        } else if (delta < 0) {
            listing.decreaseReserved(-delta);
        }

        item.setQuantity(newQuantity);

        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID cartId, UUID itemId) {
        CartEntity cart = loadCart(cartId);

        CartItemEntity item = cart.getItems().stream()
                .filter(ci -> Objects.equals(ci.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart"));

        ListingEntity listing = item.getListing();
        listing.decreaseReserved(item.getQuantity());

        cart.removeItem(item);
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    private CartEntity loadCart(UUID cartId) {
        return cartRepository.findWithItemsById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }
}
