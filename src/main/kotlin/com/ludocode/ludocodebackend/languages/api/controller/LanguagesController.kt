package com.ludocode.ludocodebackend.languages.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.app.service.LanguagesService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Languages",
    description = "Operations related to programming languages supported by the platform"
)
@RestController
@RequestMapping(ApiPaths.LANGUAGES.BASE)
class LanguagesController(private val languagesService: LanguagesService) {

    @Operation(
        summary = "Get all supported languages",
        description = """
        Returns metadata for all programming languages available on the platform.
        The returned data can be used to populate language selectors and configure
        editor and execution settings when creating or editing projects and courses.
        Requires an authenticated user session.
        """
    )
    @GetMapping
    fun getLanguages(): ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.getAllLanguages())
    }


}