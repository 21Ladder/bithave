package io.github.ladder.backend.users.service

import io.github.ladder.backend.users.persistence.UserEntity
import io.github.ladder.backend.users.persistence.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

// Resolves the signed in account, used where an action must be attributed to someone
@Component
class CurrentUserProvider(private val userRepository: UserRepository) {

    fun require(): UserEntity {
        val name = SecurityContextHolder.getContext().authentication?.name
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in")
        return userRepository.findByEmail(name)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in") }
    }
}
