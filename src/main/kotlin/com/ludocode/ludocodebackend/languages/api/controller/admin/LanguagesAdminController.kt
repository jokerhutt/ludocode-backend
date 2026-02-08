package com.ludocode.ludocodebackend.languages.api.controller.admin

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.languages.api.dto.CreateLanguageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.api.dto.UpdateLanguageRequest
import com.ludocode.ludocodebackend.languages.app.service.LanguagesService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.LANGUAGES.ADMIN_BASE)
class LanguagesAdminController(private val languagesService: LanguagesService) {


    @Operation(
        summary = "Create language",
        description = """
        Creates a new programming language definition and makes it available
        for project creation and editor configuration.
        Returns the full list of available languages after creation.
        Requires an authenticated user session.
        """
    )
    @PostMapping
    fun createLanguage (@RequestBody req: CreateLanguageRequest) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.createLanguage(req))
    }

    @Operation(
        summary = "Delete language",
        description = """
        Deletes an existing programming language definition.
        The language must not be referenced by existing projects or courses.
        Returns the updated list of available languages after the delete operation.
        Requires an authenticated user session.
        """
    )
    @DeleteMapping(ApiPaths.LANGUAGES.ID)
    fun deleteLanguage (@PathVariable id: Long) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.deleteLanguage(id))
    }

    @Operation(
        summary = "Update language",
        description = """
        Updates metadata of an existing programming language definition.
        This includes display and editor-related properties such as slug, file extension,
        icon name, base language and initial script.
        Returns the full list of available languages after the update.
        Requires an authenticated user session.
        """
    )
    @PutMapping(ApiPaths.LANGUAGES.ID)
    fun updateLanguage (@PathVariable id: Long, @RequestBody req: UpdateLanguageRequest) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.updateLanguage(id, req))
    }


}