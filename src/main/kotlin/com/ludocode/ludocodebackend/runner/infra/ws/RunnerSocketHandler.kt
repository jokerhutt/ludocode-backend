package com.ludocode.ludocodebackend.runner.infra.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonDataMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonFile
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonInitMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.RunnerRunMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.RunnerStdinMessage
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Component
class RunnerSocketHandler(
    private val pistonWebSocketClient: PistonWebSocketClient
) : TextWebSocketHandler() {

    private data class ClientBridge(
        @Volatile var pistonSession: WebSocketSession? = null,
        val pendingMessages: ConcurrentLinkedQueue<TextMessage> = ConcurrentLinkedQueue()
    )

    private val logger = LoggerFactory.getLogger(RunnerSocketHandler::class.java)
    private val mapper = jacksonObjectMapper()
    private val bridges = ConcurrentHashMap<String, ClientBridge>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        bridges[session.id] = ClientBridge()

        withMdc(LogFields.WS_SESSION_ID to session.id) {
            logger.info(LogEvents.RUNNER_WS_CLIENT_CONNECTED)
        }

        pistonWebSocketClient.connect(object : TextWebSocketHandler() {

            override fun afterConnectionEstablished(pistonSession: WebSocketSession) {
                val bridge = bridges[session.id]
                if (bridge == null || !session.isOpen) {
                    pistonSession.close()
                    return
                }

                bridge.pistonSession = pistonSession
                withMdc(
                    LogFields.WS_SESSION_ID to session.id,
                    LogFields.PISTON_WS_SESSION_ID to pistonSession.id
                ) {
                    logger.info(LogEvents.RUNNER_WS_PISTON_CONNECTED)
                }

                flushPendingMessages(session.id, session, bridge, pistonSession)
            }

            override fun handleTextMessage(pistonSession: WebSocketSession, message: TextMessage) {

                withMdc(
                    LogFields.WS_SESSION_ID to session.id,
                    LogFields.PISTON_WS_SESSION_ID to pistonSession.id
                ) {
                    logger.debug(
                        LogEvents.RUNNER_WS_PISTON_MESSAGE_FORWARDED + " {}",
                        kv(LogFields.PAYLOAD_SIZE, message.payloadLength)
                    )
                }

                if (session.isOpen) {
                    session.sendMessage(message)
                }
            }

            override fun afterConnectionClosed(pistonSession: WebSocketSession, status: CloseStatus) {
                val bridge = bridges.remove(session.id)
                bridge?.pistonSession = null
                withMdc(
                    LogFields.WS_SESSION_ID to session.id,
                    LogFields.PISTON_WS_SESSION_ID to pistonSession.id
                ) {
                    logger.info(
                        LogEvents.RUNNER_WS_PISTON_DISCONNECTED + " {} {}",
                        kv(LogFields.RESPONSE_STATUS, status.code),
                        kv(LogFields.ERROR_CODE, status.reason ?: "none")
                    )
                }

                if (session.isOpen) {
                    session.close(CloseStatus.SERVER_ERROR)
                }
            }

        }).exceptionally { ex ->
            bridges.remove(session.id)
            withMdc(LogFields.WS_SESSION_ID to session.id) {
                logger.error(
                    LogEvents.RUNNER_WS_PISTON_CONNECT_FAILED + " {}",
                    kv(LogFields.ERROR_CODE, ex.message ?: ex.javaClass.simpleName),
                    ex
                )
            }

            if (session.isOpen) {
                session.close(CloseStatus.SERVER_ERROR)
            }
            null
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

        val bridge = bridges[session.id]
        if (bridge == null) {
            withMdc(LogFields.WS_SESSION_ID to session.id) {
                logger.warn(LogEvents.RUNNER_WS_PISTON_SESSION_MISSING)
            }
            return
        }

        val pistonSession = bridge.pistonSession
        if (pistonSession == null || !pistonSession.isOpen) {
            bridge.pendingMessages.add(TextMessage(message.payload))
            withMdc(LogFields.WS_SESSION_ID to session.id) {
                logger.debug("runner_ws_message_buffered_until_piston_ready")
            }
            return
        }

        handleClientMessage(session.id, pistonSession, message)
    }

    private fun handleClientMessage(
        clientSessionId: String,
        pistonSession: WebSocketSession,
        message: TextMessage
    ) {

        withMdc(
            LogFields.WS_SESSION_ID to clientSessionId,
            LogFields.PISTON_WS_SESSION_ID to pistonSession.id
        ) {
            try {
                val node = mapper.readTree(message.payload)
                val type = node["type"]?.asText() ?: "unknown"

                when (type) {
                    "run" -> {
                        val run = mapper.treeToValue(node, RunnerRunMessage::class.java)
                        handleRun(run, pistonSession)
                    }

                    "stdin" -> {
                        val stdin = mapper.treeToValue(node, RunnerStdinMessage::class.java)
                        handleStdin(stdin, pistonSession)
                    }

                    else -> {
                        logger.warn(
                            LogEvents.RUNNER_WS_UNKNOWN_MESSAGE_TYPE + " {}",
                            kv(LogFields.MESSAGE_TYPE, type)
                        )
                    }
                }
            } catch (ex: Exception) {
                logger.warn(
                    LogEvents.RUNNER_WS_MESSAGE_PARSE_FAILED + " {}",
                    kv(LogFields.ERROR_CODE, ex.message ?: ex.javaClass.simpleName),
                    ex
                )
            }
        }
    }

    private fun flushPendingMessages(
        clientSessionId: String,
        clientSession: WebSocketSession,
        bridge: ClientBridge,
        pistonSession: WebSocketSession
    ) {
        var flushed = 0
        while (clientSession.isOpen && pistonSession.isOpen) {
            val pending = bridge.pendingMessages.poll() ?: break
            handleClientMessage(clientSessionId, pistonSession, pending)
            flushed += 1
        }

        if (flushed > 0) {
            withMdc(
                LogFields.WS_SESSION_ID to clientSessionId,
                LogFields.PISTON_WS_SESSION_ID to pistonSession.id
            ) {
                logger.debug("runner_ws_buffer_flushed {}", kv("flushedCount", flushed))
            }
        }
    }

    private fun handleRun(msg: RunnerRunMessage, pistonSession: WebSocketSession) {

        if (msg.files.isEmpty()) {
            logger.warn(
                LogEvents.RUNNER_WS_MESSAGE_PARSE_FAILED + " {}",
                kv(LogFields.ERROR_CODE, "missing_files")
            )
            return
        }

        val runtime = msg.files.first().codeLanguage

        val pistonInit = PistonInitMessage(
            language = runtime,
            version = "*",
            files = msg.files.map {
                PistonFile(
                    name = it.name,
                    content = it.content
                )
            }
        )

        pistonSession.sendMessage(
            TextMessage(mapper.writeValueAsString(pistonInit))
        )

        logger.info(
            LogEvents.RUNNER_WS_RUN_FORWARDED + " {} {}",
            kv(LogFields.FILE_COUNT, msg.files.size),
            kv(LogFields.LANGUAGE, runtime)
        )
    }

    private fun handleStdin(msg: RunnerStdinMessage, pistonSession: WebSocketSession) {

        val pistonMsg = PistonDataMessage(
            stream = "stdin",
            data = msg.text
        )

        pistonSession.sendMessage(
            TextMessage(mapper.writeValueAsString(pistonMsg))
        )

        logger.debug(
            LogEvents.RUNNER_WS_STDIN_FORWARDED + " {}",
            kv(LogFields.PAYLOAD_SIZE, msg.text.length)
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        withMdc(LogFields.WS_SESSION_ID to session.id) {
            logger.info(
                LogEvents.RUNNER_WS_CLIENT_DISCONNECTED + " {} {}",
                kv(LogFields.RESPONSE_STATUS, status.code),
                kv(LogFields.ERROR_CODE, status.reason ?: "none")
            )
        }
        bridges.remove(session.id)?.pistonSession?.close()
    }
}