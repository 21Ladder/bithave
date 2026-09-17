package io.github.ladder.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val appProperties: AppProperties) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // Angular reads the XSRF-TOKEN cookie and echoes it back as a header.
        // Null attribute name opts out of deferred loading, the plain token is what Angular sends.
        val csrfHandler = CsrfTokenRequestAttributeHandler()
        csrfHandler.setCsrfRequestAttributeName(null)

        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(csrfHandler)
                    // The webhook is authenticated by HMAC and has no browser session
                    .ignoringRequestMatchers("/api/v1/payments/btcpay/webhook")
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.GET, "/api/v1/listings/**", "/api/v1/categories", "/api/v1/btcPrice")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/carts/**", "/api/v1/checkout").permitAll()
                    .requestMatchers("/api/v1/orders/**").permitAll()
                    .requestMatchers("/api/v1/payments/btcpay/webhook").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // Order history needs an account, guest orders are reached by order number
                    .requestMatchers("/api/v1/account/**").authenticated()
                    .requestMatchers("/listingPictures/**", "/categories.json").permitAll()
                    // Only the shop operator may touch the catalogue
                    .requestMatchers(HttpMethod.POST, "/api/v1/listings").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/listings/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/listings/**").hasRole("ADMIN")
                    .anyRequest().hasRole("ADMIN")
            }
            // API only, a redirect to a login page would be useless here
            .addFilterAfter(CsrfCookieFilter(), BasicAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf(appProperties.baseUrl.trimEnd('/'))
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
    }
}
