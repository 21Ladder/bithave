package io.github.ladder.backend.users.service

import io.github.ladder.backend.users.persistence.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

// Loads accounts for the Spring Security login
@Service
class AppUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username.trim().lowercase())
            .orElseThrow { UsernameNotFoundException("No account for $username") }

        return User.withUsername(user.email)
            .password(user.passwordHash)
            .authorities(listOf(SimpleGrantedAuthority(user.role.authority)))
            .build()
    }
}
