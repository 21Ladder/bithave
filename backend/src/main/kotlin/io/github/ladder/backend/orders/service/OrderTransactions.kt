package io.github.ladder.backend.orders.service

import io.github.ladder.backend.listings.domain.ListingStatus
import io.github.ladder.backend.orders.domain.CartStatus
import io.github.ladder.backend.orders.domain.OrderStatus
import io.github.ladder.backend.orders.dto.CheckoutRequest
import io.github.ladder.backend.orders.persistence.CartRepository
import io.github.ladder.backend.orders.persistence.OrderEntity
import io.github.ladder.backend.orders.persistence.OrderItemEntity
import io.github.ladder.backend.orders.persistence.OrderRepository
import io.github.ladder.backend.orders.persistence.ShippingAddress
import io.github.ladder.backend.users.persistence.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

// Separate bean so each step runs in its own transaction, self-invocation would bypass the proxy
@Service
class OrderTransactions(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val orderNumberGenerator: OrderNumberGenerator,
    private val userRepository: UserRepository
) {

    // Snapshots prices and locks the cart, so the invoice can no longer drift from its contents
    @Transactional
    fun createPendingOrder(cartId: UUID, request: CheckoutRequest): OrderEntity {
        val cart = cartRepository.findWithItemsById(cartId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found") }

        if (cart.status != CartStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cart is no longer active")
        }
        if (cart.items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty")
        }

        val address = request.shippingAddress
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address is required")

        val order = OrderEntity(
            orderNumber = uniqueOrderNumber(),
            cart = cart,
            email = request.email!!.trim().lowercase(),
            shippingAddress = ShippingAddress(
                name = address.name!!.trim(),
                street = address.street!!.trim(),
                postalCode = address.postalCode!!.trim(),
                city = address.city!!.trim(),
                country = address.country!!.trim().uppercase()
            ),
            totalCents = 0,
            currency = "EUR"
        )

        var total = 0L
        cart.items.forEach { item ->
            val listing = item.listing
            if (listing.status != ListingStatus.ACTIVE) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "\"${listing.title}\" is no longer available")
            }
            order.addItem(
                OrderItemEntity(
                    listingId = listing.id!!,
                    titleSnapshot = listing.title,
                    unitPriceCents = item.unitPriceCents,
                    quantity = item.quantity
                )
            )
            total += item.unitPriceCents * item.quantity
        }

        if (total <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart total must be greater than zero")
        }
        order.totalCents = total

        // Guest checkout stays anonymous, a signed in buyer gets the order in their history
        currentUserEmail()?.let { email ->
            userRepository.findByEmail(email).ifPresent { order.user = it }
        }

        cart.status = CartStatus.CONVERTED
        cartRepository.save(cart)
        return orderRepository.save(order)
    }

    @Transactional
    fun attachInvoice(orderId: UUID, invoiceId: String) {
        val order = orderRepository.findById(orderId).orElseThrow()
        order.btcpayInvoiceId = invoiceId
        orderRepository.save(order)
    }

    // Releases the cart again if the invoice could never be created
    @Transactional
    fun cancelOrderAndReleaseCart(orderId: UUID) {
        val order = orderRepository.findById(orderId).orElseThrow()
        order.transitionTo(OrderStatus.CANCELLED)
        order.cart?.let {
            it.status = CartStatus.ACTIVE
            cartRepository.save(it)
        }
        orderRepository.save(order)
    }

    private fun currentUserEmail(): String? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (!authentication.isAuthenticated || authentication.name == "anonymousUser") return null
        return authentication.name
    }

    private fun uniqueOrderNumber(): String {
        repeat(5) {
            val candidate = orderNumberGenerator.next()
            if (!orderRepository.existsByOrderNumber(candidate)) return candidate
        }
        throw IllegalStateException("Could not generate a unique order number")
    }
}
