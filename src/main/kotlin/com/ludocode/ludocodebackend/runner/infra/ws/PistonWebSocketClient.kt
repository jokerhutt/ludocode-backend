package com.ludocode.ludocodebackend.runner.infra.ws

import com.ludocode.ludocodebackend.commons.constants.ExternalPathConstants
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.runner.configuration.PistonProperties
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(PistonWebSocketClient::class.java)
    private val client = StandardWebSocketClient()

    val pistonBase = pistonProperties.base
    val executePath = "$pistonBase${ExternalPathConstants.PISTON_CONNECT}"


    fun connect(handler: WebSocketHandler): CompletableFuture<WebSocketSession> {
        logger.debug(
            LogEvents.RUNNER_WS_PISTON_CONNECTING + " {}",
            kv(LogFields.URI_PATH, executePath)
        )

        return client.execute(handler, executePath)
            .whenComplete { _, ex ->
                if (ex != null) {
                    logger.error(
                        LogEvents.RUNNER_WS_PISTON_CONNECT_FAILED + " {} {}",
                        kv(LogFields.URI_PATH, executePath),
                        kv(LogFields.ERROR_CODE, ex.message ?: ex.javaClass.simpleName),
                        ex
                    )
                }
            }
    }
}