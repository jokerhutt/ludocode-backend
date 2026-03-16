package com.ludocode.ludocodebackend.runner.infra.ws

import com.ludocode.ludocodebackend.commons.constants.ExternalPathConstants
import com.ludocode.ludocodebackend.runner.configuration.PistonProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.util.concurrent.CompletableFuture

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Component
class PistonWebSocketClient(
    private val pistonProperties: PistonProperties
) {
    private val client = StandardWebSocketClient()

    val pistonBase = pistonProperties.base
    val executePath = "$pistonBase${ExternalPathConstants.PISTON_CONNECT}"


    fun connect(handler: WebSocketHandler): CompletableFuture<WebSocketSession> {
        return client.execute(handler, executePath)
    }
}