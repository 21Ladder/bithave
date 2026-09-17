package io.github.ladder.backend.orders.service

import io.github.ladder.backend.config.AppProperties
import io.github.ladder.backend.listings.persistence.CategoryEntity
import io.github.ladder.backend.listings.persistence.ListingEntity
import io.github.ladder.backend.listings.persistence.ListingRepository
import io.github.ladder.backend.orders.dto.CartItemAddRequest
import io.github.ladder.backend.orders.dto.CartResponse
import io.github.ladder.backend.orders.mapper.CartMapper
import io.github.ladder.backend.orders.persistence.CartEntity
import io.github.ladder.backend.orders.persistence.CartItemEntity
import io.github.ladder.backend.orders.persistence.CartRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CartServiceImplTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var listingRepository: ListingRepository
    private lateinit var cartMapper: CartMapper
    private lateinit var appProperties: AppProperties
    private lateinit var cartService: CartServiceImpl

    @BeforeEach
    fun setUp() {
        cartRepository = mock(CartRepository::class.java)
        listingRepository = mock(ListingRepository::class.java)
        cartMapper = mock(CartMapper::class.java)
        appProperties = AppProperties()
        cartService = CartServiceImpl(cartRepository, listingRepository, cartMapper, appProperties, mock(CartOwnership::class.java))
    }

    @Test
    fun addItem_withValidRequest_addsNewItemAndReservesQuantity() {
        val cartId = UUID.randomUUID()
        val listingId = UUID.randomUUID()
        val cart = CartEntity()
        `when`(cartRepository.findWithItemsById(cartId)).thenReturn(Optional.of(cart))

        val category = CategoryEntity("electronics", null)
        val listing = ListingEntity("Camera", 1_500, 5, category, listOf(), UUID.randomUUID())
        `when`(listingRepository.findByIdForUpdate(listingId)).thenReturn(Optional.of(listing))

        val expectedResponse = CartResponse(cartId, listOf(), 2, 3_000)
        `when`(cartMapper.toResponse(cart)).thenReturn(expectedResponse)

        val request = CartItemAddRequest(listingId, 2)

        val response = cartService.addItem(cartId, request)

        assertEquals(1, cart.items.size)
        val savedItem: CartItemEntity = cart.items[0]
        assertSame(listing, savedItem.listing)
        assertEquals(2, savedItem.quantity)
        assertEquals(listing.priceCents, savedItem.unitPriceCents)
        assertEquals(2, listing.reservedQuantity)

        verify(cartRepository).save(cart)
        verify(cartMapper).toResponse(cart)

        assertSame(expectedResponse, response)
    }
}
