package com.ludocode.ludocodebackend.languages.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.languages.api.dto.CreateLanguageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.api.dto.UpdateLanguageRequest
import com.ludocode.ludocodebackend.languages.app.service.LanguagesService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiPaths.LANGUAGES.BASE)
class LanguagesController(private val languagesService: LanguagesService) {

    @GetMapping
    fun getLanguages () : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.getAllLanguages())
    }

    @PostMapping
    fun createLanguage (@RequestBody req: CreateLanguageRequest) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.createLanguage(req))
    }

    @DeleteMapping(ApiPaths.LANGUAGES.ID)
    fun deleteLanguage (@PathVariable id: Long) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.deleteLanguage(id))
    }

    @PutMapping(ApiPaths.LANGUAGES.ID)
    fun updateLanguage (@PathVariable id: Long, @RequestBody req: UpdateLanguageRequest) : ResponseEntity<List<LanguageMetadata>> {
        return ResponseEntity.ok(languagesService.updateLanguage(id, req))
    }

}