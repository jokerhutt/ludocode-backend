package com.ludocode.ludocodebackend.playground.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.service.CodeRunnerService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping(ApiPaths.RUNNER.BASE)
class CodeRunnerController(private val codeRunnerService: CodeRunnerService) {


    @PostMapping(ApiPaths.RUNNER.EXECUTE)
    fun runProject (@RequestBody request: ProjectSnapshot, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<RunnerResult> {
        return ResponseEntity.ok(codeRunnerService.runCode(request))
    }

}