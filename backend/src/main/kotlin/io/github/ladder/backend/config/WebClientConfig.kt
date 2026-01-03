package io.github.ladder.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(private val btcPayProperties: BtcPayProperties) {

    @Bean
    fun btcpayWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(btcPayProperties.url.removeSuffix("/"))
            .defaultHeader("Authorization", "token ${btcPayProperties.apiToken}")
            .build()
}
