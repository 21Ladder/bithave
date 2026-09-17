package io.github.ladder.backend.orders.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ShippingAddressRequest(
    @field:NotBlank @field:Size(max = 160) val name: String?,
    @field:NotBlank @field:Size(max = 255) val street: String?,
    @field:NotBlank @field:Size(max = 32) val postalCode: String?,
    @field:NotBlank @field:Size(max = 160) val city: String?,
    @field:NotBlank @field:Pattern(regexp = "[A-Za-z]{2}", message = "must be a two letter country code")
    val country: String?
)
