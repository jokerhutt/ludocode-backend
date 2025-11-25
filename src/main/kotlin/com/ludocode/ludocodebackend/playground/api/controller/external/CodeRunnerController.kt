package com.ludocode.ludocodebackend.playground.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.service.CodeRunnerService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.RUNNER)
class CodeRunnerController(private val codeRunnerService: CodeRunnerService) {


    @PostMapping(PathConstants.RUN_PROJECT)
    fun runProject (@RequestBody request: ProjectSnapshot, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<RunnerResult> {
        return ResponseEntity.ok(codeRunnerService.runCode(request))
    }

}