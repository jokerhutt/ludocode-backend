package com.ludocode.ludocodebackend.languages.api.dto

import com.ludocode.ludocodebackend.languages.domain.enums.LanguageRuntime

data class LanguageMetadata(
    val languageId: Long,
    val name: String,
    val slug: String,
    val editorId: String,
    val pistonId: String,
    val runtime: LanguageRuntime = LanguageRuntime.PISTON,
    val runtimeVersion: String,
    val extension: String,
    val base: String,
    val iconName: String,
    val initialScript: String,
    val enabled: Boolean,
    val disabledReason: String?
)