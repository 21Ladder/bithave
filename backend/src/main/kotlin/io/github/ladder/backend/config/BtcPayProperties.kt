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

    // Shared secret for webhook signature checks, empty means webhooks are rejected
    var webhookSecret: String = ""

    // How long a buyer has to pay before the invoice expires
    var invoiceExpirationMinutes: Int = 30
}
