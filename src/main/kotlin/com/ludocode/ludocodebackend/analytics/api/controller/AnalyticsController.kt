package com.ludocode.ludocodebackend.analytics.api.controller
import com.ludocode.ludocodebackend.analytics.api.dto.AnalyticsEventRequest
import com.ludocode.ludocodebackend.analytics.app.service.AnalyticsService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.ANALYTICS.BASE)
class AnalyticsController(private val analyticsService: AnalyticsService) {

    @PostMapping
    fun track(@RequestBody req: AnalyticsEventRequest): ResponseEntity<Void> {
        analyticsService.saveEvent(req)
        return ResponseEntity.noContent().build()
    }

}