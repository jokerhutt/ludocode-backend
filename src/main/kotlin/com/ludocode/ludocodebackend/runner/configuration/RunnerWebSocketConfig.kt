package com.ludocode.ludocodebackend.runner.configuration

import com.ludocode.ludocodebackend.commons.configuration.web.CorsProperties
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.runner.infra.ws.RunnerSocketHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Configuration
@EnableConfigurationProperties(CorsProperties::class)
@EnableWebSocket
class RunnerWebSocketConfig (
    private val handler: RunnerSocketHandler,
    private val corsProperties: CorsProperties
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, ApiPaths.RUNNER.WS_BASE)
            .setAllowedOriginPatterns(*corsProperties.origins.toTypedArray())

    }

}