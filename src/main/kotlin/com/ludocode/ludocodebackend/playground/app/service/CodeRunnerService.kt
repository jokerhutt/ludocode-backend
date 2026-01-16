package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonFile
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Service
class CodeRunnerService(private val pistonOutboundPort: PistonOutboundPort) {

    private val logger = LoggerFactory.getLogger(CodeRunnerService::class.java)

    internal fun runCode (project: ProjectSnapshot) : RunnerResult {

        val runtime = project.projectLanguage.name
        val fileNames = project.files.map { it.path }

        val req = PistonRequest(
            language = runtime,
            files = project.files.map { PistonFile(name = it.path, content = it.content) },
            stdin = "",
            args = emptyList(),
            run_timeout = 3000
        )

        val resp = pistonOutboundPort.execute(req)

        val run = resp.run

        if (run == null) {
            logger.error(
                LogEvents.RUNNER_EXECUTE_FAILED + " {} {}",
                kv(LogFields.LANGUAGE, runtime),
                kv(LogFields.FILE_COUNT, fileNames.size)
            )
            return RunnerResult("", "Piston run is null", -1)
        }

        if (run.code != 0) {
            logger.warn(
                LogEvents.RUNNER_EXECUTE_NONZERO_EXIT + " {} {} {}",
                kv(LogFields.LANGUAGE, runtime),
                kv(LogFields.EXIT_CODE, run.code),
                kv(LogFields.FILE_COUNT, fileNames.size),
            )
        }

        return RunnerResult(
            stdout = run.stdout,
            stderr = run.stderr,
            exitCode = run.code,
        )

    }

}