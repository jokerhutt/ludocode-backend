package com.ludocode.ludocodebackend.runner.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonFile
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonRequest
import com.ludocode.ludocodebackend.runner.api.dto.response.RunnerResult
import com.ludocode.ludocodebackend.runner.app.port.out.PistonOutboundPort
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@Service
class RunnerService(private val pistonOutboundPort: PistonOutboundPort) {

    private val logger = LoggerFactory.getLogger(RunnerService::class.java)

    internal fun runCode(project: ProjectSnapshot): RunnerResult {

        val runtime = project.projectLanguage.pistonId
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
                StructuredArguments.kv(LogFields.LANGUAGE, runtime),
                StructuredArguments.kv(LogFields.FILE_COUNT, fileNames.size)
            )
            return RunnerResult("", "Piston run is null", -1)
        }

        if (run.code != 0) {
            logger.warn(
                LogEvents.RUNNER_EXECUTE_NONZERO_EXIT + " {} {} {}",
                StructuredArguments.kv(LogFields.LANGUAGE, runtime),
                StructuredArguments.kv(LogFields.EXIT_CODE, run.code),
                StructuredArguments.kv(LogFields.FILE_COUNT, fileNames.size),
            )
        }

        return RunnerResult(
            stdout = run.stdout,
            stderr = run.stderr,
            exitCode = run.code,
        )

    }

}