package com.ludocode.ludocodebackend.runner.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.runner.api.dto.response.RunnerResult
import com.ludocode.ludocodebackend.runner.app.service.RunnerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "Code Runner",
    description = "Operations related to executing code on the code runner"
)
@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping(ApiPaths.RUNNER.BASE)
class RunnerController(private val runnerService: RunnerService) {


    @Operation(
        summary = "Execute project code",
        description = """
        Executes the code contained in the provided project snapshot.
        Runs the project in an isolated execution environment.
        Returns the program's standard output, standard error, and exit code.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping(ApiPaths.RUNNER.EXECUTE)
    fun runProject(
        @RequestBody request: ProjectSnapshot,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<RunnerResult> {
        return ResponseEntity.ok(runnerService.runCode(request))
    }

}