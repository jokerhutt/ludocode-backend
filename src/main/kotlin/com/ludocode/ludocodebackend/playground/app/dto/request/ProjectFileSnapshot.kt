package com.ludocode.ludocodebackend.playground.app.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType

data class ProjectFileSnapshot(
    val path: String,
    val language: LanguageType,
    val content: String
)
