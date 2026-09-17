package io.github.ladder.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppProperties {
    // Public base URL of the shop, used to build the BTCPay return link
    var baseUrl: String = "http://localhost:4200"

    // How long an untouched cart keeps its stock reservation
    var cartTtlMinutes: Long = 60
}
