package io.github.ladder.backend.users.persistence

import io.github.ladder.backend.users.domain.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity() {

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null

    @Column(nullable = false, length = 255)
    var email: String = ""

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var role: UserRole = UserRole.CUSTOMER

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    constructor(email: String, passwordHash: String, role: UserRole) : this() {
        this.email = email
        this.passwordHash = passwordHash
        this.role = role
    }
}
