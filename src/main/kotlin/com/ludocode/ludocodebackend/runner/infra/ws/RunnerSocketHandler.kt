package com.ludocode.ludocodebackend.runner.infra.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonDataMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonFile
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonInitMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.RunnerRunMessage
import com.ludocode.ludocodebackend.runner.api.dto.request.RunnerStdinMessage
import com.ludocode.ludocodebackend.runner.infra.ws.PistonWebSocketClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Component
class RunnerSocketHandler(
    private val pistonWebSocketClient: PistonWebSocketClient
) : TextWebSocketHandler() {

    private val mapper = jacksonObjectMapper()
    private val pistonSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {

        pistonWebSocketClient.connect(object : TextWebSocketHandler() {

            override fun afterConnectionEstablished(pistonSession: WebSocketSession) {
                pistonSessions[session.id] = pistonSession
            }

            override fun handleTextMessage(pistonSession: WebSocketSession, message: TextMessage) {

                println("Piston → ${message.payload}")

                if (session.isOpen) {
                    session.sendMessage(message)
                }
            }

            override fun afterConnectionClosed(pistonSession: WebSocketSession, status: CloseStatus) {
                pistonSessions.remove(session.id)
            }

        }).exceptionally { ex ->
            println("Piston connection failed: ${ex.message}")
            ex.printStackTrace()

            session.close(CloseStatus.SERVER_ERROR)
            null
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

        val pistonSession = pistonSessions[session.id] ?: return

        val node = mapper.readTree(message.payload)
        val type = node["type"].asText()

        when (type) {

            "run" -> {
                val run = mapper.treeToValue(node, RunnerRunMessage::class.java)
                handleRun(run, pistonSession)
            }

            "stdin" -> {
                val stdin = mapper.treeToValue(node, RunnerStdinMessage::class.java)
                handleStdin(stdin, pistonSession)
            }
        }
    }

    private fun handleRun(msg: RunnerRunMessage, pistonSession: WebSocketSession) {

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
    }

    private fun handleStdin(msg: RunnerStdinMessage, pistonSession: WebSocketSession) {

        val pistonMsg = PistonDataMessage(
            stream = "stdin",
            data = msg.text
        )

        pistonSession.sendMessage(
            TextMessage(mapper.writeValueAsString(pistonMsg))
        )
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        pistonSessions.remove(session.id)?.close()
    }
}