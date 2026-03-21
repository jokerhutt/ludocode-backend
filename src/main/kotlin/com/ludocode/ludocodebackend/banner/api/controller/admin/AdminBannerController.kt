package com.ludocode.ludocodebackend.banner.api.controller.admin

import com.ludocode.ludocodebackend.banner.api.dto.BannerMetadata
import com.ludocode.ludocodebackend.banner.api.dto.CreateBannerRequest
import com.ludocode.ludocodebackend.banner.app.service.BannerService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Admin Banners",
    description = "Admin operations for creating, deleting and listing banners"
)
@Profile("admin", "dev", "test", "devtestadmin")
@RestController
@RequestMapping(ApiPaths.BANNERS.ADMIN_BASE)
class AdminBannerController(
    private val bannerService: BannerService
) {

    @Operation(
        summary = "Create banner",
        description = "Creates a new active banner. Only one active banner per type is allowed."
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    fun createBanner(@RequestBody req: CreateBannerRequest): ResponseEntity<List<BannerMetadata>> {
        return ResponseEntity.ok(bannerService.createBanner(req))
    }

    @Operation(
        summary = "Delete banner",
        description = "Soft deletes the banner by setting isActive=false."
    )
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping(ApiPaths.BANNERS.ID)
    fun deleteBanner(@PathVariable bannerId: Long): ResponseEntity<List<BannerMetadata>> {
        return ResponseEntity.ok(bannerService.deleteBanner(bannerId))
    }

    @Operation(
        summary = "List banners",
        description = "Returns all banners including inactive ones."
    )
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    fun getAllBanners(): ResponseEntity<List<BannerMetadata>> {
        return ResponseEntity.ok(bannerService.getAllBanners())
    }
}

