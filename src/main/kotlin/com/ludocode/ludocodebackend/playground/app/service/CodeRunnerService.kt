package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonFile
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Service
class CodeRunnerService(private val pistonOutboundPort: PistonOutboundPort) {

    internal fun runCode (project: ProjectSnapshot) : RunnerResult {

        val runtime = project.projectLanguage.name

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