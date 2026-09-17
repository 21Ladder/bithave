package io.github.ladder.backend.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig(private val btcPayProperties: BtcPayProperties) {

    // Timeouts keep a slow BTCPay from pinning request threads indefinitely
    @Bean
    fun btcpayWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(10)) }

        return WebClient.builder()
            .baseUrl(btcPayProperties.url.removeSuffix("/"))
            .defaultHeader("Authorization", "token ${btcPayProperties.apiToken}")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
