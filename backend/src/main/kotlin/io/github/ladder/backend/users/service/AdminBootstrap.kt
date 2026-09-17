package io.github.ladder.backend.users.service

import io.github.ladder.backend.users.domain.UserRole
import io.github.ladder.backend.users.persistence.UserEntity
import io.github.ladder.backend.users.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

// Creates the shop operator account on first start, there is no public admin signup
@Component
class AdminBootstrap(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin-email:}") private val adminEmail: String,
    @Value("\${app.admin-password:}") private val adminPassword: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("No admin credentials configured, skipping admin bootstrap")
            return
        }

        val email = adminEmail.trim().lowercase()
        if (userRepository.existsByEmail(email)) return

        userRepository.save(UserEntity(email, passwordEncoder.encode(adminPassword), UserRole.ADMIN))
        log.info("Created admin account {}", email)
    }
}
