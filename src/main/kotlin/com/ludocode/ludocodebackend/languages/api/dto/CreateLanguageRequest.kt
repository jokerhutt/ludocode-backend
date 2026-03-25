package com.ludocode.ludocodebackend.languages.api.dto

import com.ludocode.ludocodebackend.languages.domain.enums.LanguageRuntime

data class CreateLanguageRequest(
    val name: String,
    val slug: String,
    val editorId: String,
    val pistonId: String,
    val runtime: LanguageRuntime,
    val extension: String,
    val runtimeVersion: String = "*",
    val base: String,
    val iconName: String,
    val initialScript: String
)