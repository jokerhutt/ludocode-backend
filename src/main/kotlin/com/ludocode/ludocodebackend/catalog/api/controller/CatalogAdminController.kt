package com.ludocode.ludocodebackend.catalog.api.controller

import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ModuleDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCModuleResponse
import com.ludocode.ludocodebackend.catalog.app.service.CatalogChangeService
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PathConstants.ADMIN)
class CatalogAdminController(private val catalogChangeService: CatalogChangeService) {


    @PostMapping(PathConstants.CHANGE_CATALOG)
    fun changeCatalog (@RequestBody req: ModuleDiffRequest) : ResponseEntity<CCModuleResponse> {
        return ResponseEntity.ok(catalogChangeService.applyLessonDif(req))
    }



}