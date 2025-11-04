package io.github.ladder.backend.orders.service;

import io.github.ladder.backend.listings.persistence.CategoryEntity;
import io.github.ladder.backend.listings.persistence.ListingEntity;
import io.github.ladder.backend.listings.persistence.ListingRepository;
import io.github.ladder.backend.orders.dto.CartItemAddRequest;
import io.github.ladder.backend.orders.dto.CartResponse;
import io.github.ladder.backend.orders.mapper.CartMapper;
import io.github.ladder.backend.orders.persistence.CartEntity;
import io.github.ladder.backend.orders.persistence.CartItemEntity;
import io.github.ladder.backend.orders.persistence.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    // dependencies to be mocked later on
    private CartRepository cartRepository;
    private ListingRepository listingRepository;
    private CartMapper cartMapper;
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        // create fresh mocks before each test
        cartRepository = mock(CartRepository.class);
        listingRepository = mock(ListingRepository.class);
        cartMapper = mock(CartMapper.class);
        // service under test with mocks
        cartService = new CartServiceImpl(cartRepository, listingRepository, cartMapper);
    }

    // Given an existing cart and a listing with stock, when I add 2 units to the cart, the cart gets a new item, the listing’s reserved quantity increases, the cart is saved, and the mapped response is returned.
    @Test
    void addItem_withValidRequest_addsNewItemAndReservesQuantity() {
        // ids and an empty cart returned by the repository
        UUID cartId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        CartEntity cart = new CartEntity();
        when(cartRepository.findWithItemsById(cartId)).thenReturn(Optional.of(cart));

        // listing from the listing repository with enough stock
        CategoryEntity category = new CategoryEntity("electronics", null);
        ListingEntity listing = new ListingEntity("Camera", 1_500, 5, category, List.of(), UUID.randomUUID());
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        // mapper returns a simple response dto
        CartResponse expectedResponse = new CartResponse(cartId, List.of(), 2, 3_000);
        when(cartMapper.toResponse(cart)).thenReturn(expectedResponse);

        // call the cart service to add item and capture the response
        CartItemAddRequest request = new CartItemAddRequest(listingId, 2);

        CartResponse response = cartService.addItem(cartId, request);

        // assert that the cart has one item with correct values and listing reserved count increased
        assertEquals(1, cart.getItems().size());
        CartItemEntity savedItem = cart.getItems().get(0);
        assertSame(listing, savedItem.getListing());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(listing.getPriceUsd(), savedItem.getUnitPriceUsd());
        assertEquals(2, listing.getReservedQuantity());

        // verify that the repository saves and mapper call has happpened
        verify(cartRepository).save(cart);
        verify(cartMapper).toResponse(cart);

        // assert that the expected response is the actual response
        assertSame(expectedResponse, response);
    }
}
