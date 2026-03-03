package com.ludocode.ludocodebackend.auth.api.controller.admin

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.AUTH.ADMIN_BASE)
class AuthAdminController {

    @GetMapping(ApiPaths.AUTH.CHECK)
    fun checkAdminAuthentication(): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("ok" to true))
    }

}