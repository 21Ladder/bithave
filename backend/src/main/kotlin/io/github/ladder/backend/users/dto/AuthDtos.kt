package io.github.ladder.backend.users.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Sign up with an email and a password
data class RegisterRequest(
    @field:NotBlank @field:Email val email: String?,
    @field:NotBlank @field:Size(min = 10, max = 128, message = "must be at least 10 characters")
    val password: String?
)

data class LoginRequest(
    @field:NotBlank @field:Email val email: String?,
    @field:NotBlank val password: String?
)

// Who is currently logged in, used by the frontend to shape the UI
data class CurrentUserResponse(
    val email: String,
    val role: String
)
