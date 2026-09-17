package io.github.ladder.backend.users.domain

// CUSTOMER buys, ADMIN runs the shop. Single seller setup, so there is no SELLER role.
enum class UserRole {
    CUSTOMER,
    ADMIN;

    val authority: String
        get() = "ROLE_$name"
}
