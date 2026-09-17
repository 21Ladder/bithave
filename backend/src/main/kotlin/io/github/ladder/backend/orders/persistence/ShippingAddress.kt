package io.github.ladder.backend.orders.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ShippingAddress(
    @Column(name = "shipping_name", nullable = false, length = 160)
    var name: String = "",

    @Column(name = "shipping_street", nullable = false, length = 255)
    var street: String = "",

    @Column(name = "shipping_postal_code", nullable = false, length = 32)
    var postalCode: String = "",

    @Column(name = "shipping_city", nullable = false, length = 160)
    var city: String = "",

    @Column(name = "shipping_country", nullable = false, length = 2)
    var country: String = ""
)
