package com.ludocode.ludocodebackend.playground.app.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.util.UUID

data class ProjectFileSnapshot(
    val id: UUID?,
    var path: String,
    val language: LanguageType,
    var content: String
)
