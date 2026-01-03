package io.github.ladder.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "btcpay")
class BtcPayProperties {
    lateinit var url: String
    lateinit var storeId: String
    lateinit var apiToken: String
}
