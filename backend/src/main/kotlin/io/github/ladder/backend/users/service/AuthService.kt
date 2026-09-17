package io.github.ladder.backend.users.service

import io.github.ladder.backend.users.domain.UserRole
import io.github.ladder.backend.users.dto.CurrentUserResponse
import io.github.ladder.backend.users.dto.RegisterRequest
import io.github.ladder.backend.users.persistence.UserEntity
import io.github.ladder.backend.users.persistence.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

// Registration and account lookups. Login itself runs through Spring Security.
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun register(request: RegisterRequest): CurrentUserResponse {
        val email = request.email!!.trim().lowercase()
        if (userRepository.existsByEmail(email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered")
        }

        // Self service registration always creates a customer, never an admin
        val user = userRepository.save(
            UserEntity(
                email = email,
                passwordHash = passwordEncoder.encode(request.password!!),
                role = UserRole.CUSTOMER
            )
        )
        return CurrentUserResponse(user.email, user.role.name)
    }

    @Transactional(readOnly = true)
    fun currentUser(email: String): CurrentUserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in") }
        return CurrentUserResponse(user.email, user.role.name)
    }
}
