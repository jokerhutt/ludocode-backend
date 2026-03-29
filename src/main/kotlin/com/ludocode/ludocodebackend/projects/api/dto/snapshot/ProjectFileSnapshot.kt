package com.ludocode.ludocodebackend.projects.api.dto.snapshot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectFileSnapshot(
    val id: UUID?,
    var path: String,
    val language: String,
    var content: String
)