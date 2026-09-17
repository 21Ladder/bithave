package io.github.ladder.backend.orders.service

import io.github.ladder.backend.orders.persistence.CartEntity
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

// Ties a cart to the browser session that created it
@Component
class CartOwnership(private val request: HttpServletRequest) {

    // Creates the session on demand, the id is what we store on the cart
    fun currentSessionId(): String = request.getSession(true).id

    fun claim(cart: CartEntity) {
        cart.sessionId = currentSessionId()
    }

    // Older carts have no owner yet, adopt them instead of locking the buyer out
    fun assertOwned(cart: CartEntity) {
        val owner = cart.sessionId
        if (owner == null) {
            claim(cart)
            return
        }
        if (owner != currentSessionId()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found")
        }
    }
}
