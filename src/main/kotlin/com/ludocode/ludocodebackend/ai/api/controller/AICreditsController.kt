package com.ludocode.ludocodebackend.ai.api.controller

import com.ludocode.ludocodebackend.ai.app.service.AICreditService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(
    name = "AI Credits",
    description = "Operations related to user AI credits"
)
@ConditionalOnProperty(prefix = "ai", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping(ApiPaths.CREDITS.BASE)
class AICreditsController(private val aICreditService: AICreditService) {

    @Operation(summary = "Get AI credit balance",
        description = """
        Returns the remaining AI credit balance for the currently authenticated user.
        Requires a valid session cookie. 
        """
        )
    @GetMapping
    fun getAiCredits(@AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<Int> {
        return ResponseEntity.ok(aICreditService.initializeOrGetCredits(userId).credits)
    }

}