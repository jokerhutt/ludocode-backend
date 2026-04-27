package com.ludocode.ludocodebackend.auth.api.controller.admin

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Auth",
    description = "Operations related to admin authentication"
)
@RestController
@RequestMapping(ApiPaths.AUTH.ADMIN_BASE)
class AuthAdminController {

    @Operation(
        summary = "Check admin authentication status",
        description = """
        Verifies that the current request is authorized as an admin.
        Access to this endpoint is restricted by security configuration on the `/admin` path.
        If the request reaches this controller, the user is already authenticated as an admin.
        Returns a simple success response for frontend access checks.
        """
    )
    @GetMapping(ApiPaths.AUTH.CHECK)
    fun checkAdminAuthentication(): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("ok" to true))
    }

}