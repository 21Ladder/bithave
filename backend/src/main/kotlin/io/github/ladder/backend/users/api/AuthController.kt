package io.github.ladder.backend.users.api

import io.github.ladder.backend.users.dto.CurrentUserResponse
import io.github.ladder.backend.users.dto.LoginRequest
import io.github.ladder.backend.users.dto.RegisterRequest
import io.github.ladder.backend.users.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

// Session based auth, the browser keeps an HttpOnly cookie
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val authenticationManager: AuthenticationManager
) {

    private val securityContextRepository = HttpSessionSecurityContextRepository()

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody @Valid request: RegisterRequest): CurrentUserResponse =
        authService.register(request)

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): CurrentUserResponse {
        val authentication = try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email!!.trim().lowercase(), request.password)
            )
        } catch (ex: BadCredentialsException) {
            // Same message for unknown email and wrong password, so accounts cannot be probed
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        securityContextRepository.saveContext(context, httpRequest, httpResponse)

        return authService.currentUser(authentication.name)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(httpRequest: HttpServletRequest) {
        httpRequest.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
    }

    @GetMapping("/me")
    fun me(authentication: Authentication?): CurrentUserResponse {
        val name = authentication?.name
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in")
        return authService.currentUser(name)
    }
}
