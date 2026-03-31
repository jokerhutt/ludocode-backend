package com.ludocode.ludocodebackend.maintenance

import com.ludocode.ludocodebackend.commons.configuration.app.MaintenanceProperties
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.MAINTENANCE.BASE)
class MaintenanceController(
    private val maintenanceProperties: MaintenanceProperties
) {

    @GetMapping
    fun getMaintenanceStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf("enabled" to maintenanceProperties.enabled)
        )
    }
}

