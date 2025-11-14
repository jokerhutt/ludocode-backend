package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.playground.app.dto.client.PistonFile
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.stereotype.Service

@Service
class CodeRunnerService(private val pistonOutboundPort: PistonOutboundPort) {

    fun runCode (project: ProjectSnapshot) : RunnerResult {

        val runtime = when (project.projectLanguage) {
            LanguageType.python -> "python-3.10.0"
            LanguageType.web -> "node-18.0.0"
            else -> error("Unsupported Language")
        }

        val pistonFiles = project.files.map { file ->
            PistonFile(
                name = file.path,
                content = file.content
            )
        }

        val req = PistonRequest(
            language = runtime,
            files = pistonFiles,
            stdin = "",
            args = emptyList(),
            run_timeout = 3000
        )

        val resp = pistonOutboundPort.execute(req)

        val run = resp.run ?: return RunnerResult(
            stdout = "",
            stderr = "Piston run is null",
            exitCode = -1,
        )

        return RunnerResult(
            stdout = run.stdout,
            stderr = run.stderr,
            exitCode = run.code,
        )

    }

}