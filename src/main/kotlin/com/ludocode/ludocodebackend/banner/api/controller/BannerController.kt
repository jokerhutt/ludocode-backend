package com.ludocode.ludocodebackend.banner.api.controller

import com.ludocode.ludocodebackend.banner.api.dto.BannerActiveRequest
import com.ludocode.ludocodebackend.banner.app.service.BannerService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Banners",
    description = "Public operations for retrieving active banners"
)
@RestController
@RequestMapping(ApiPaths.BANNERS.BASE)
class BannerController(
    private val bannerService: BannerService
) {

    @Operation(
        summary = "Get active banners",
        description = "Returns the list of currently active banners."
    )
    @GetMapping
    fun getActiveBanners(): ResponseEntity<List<BannerActiveRequest>> {
        return ResponseEntity.ok(bannerService.getActiveBanners())
    }
}

