package com.ludocode.ludocodebackend.playground.api.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.util.UUID


@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectFileSnapshot(
    val id: UUID?,
    var path: String,
    val language: LanguageMetadata,
    var content: String
)
