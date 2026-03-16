package com.ludocode.ludocodebackend.runner.configuration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.runner.infra.ws.RunnerSocketHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Configuration
@EnableWebSocket
class RunnerWebSocketConfig (
    private val handler: RunnerSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, ApiPaths.RUNNER.WS_BASE)
    }

}