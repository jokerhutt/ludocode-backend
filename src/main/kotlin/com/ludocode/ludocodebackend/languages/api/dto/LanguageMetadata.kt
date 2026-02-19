package com.ludocode.ludocodebackend.languages.api.dto

data class LanguageMetadata(
    val languageId: Long,
    val name: String,
    val slug: String,
    val editorId: String,
    val pistonId: String,
    val extension: String,
    val base: String,
    val iconName: String,
    val initialScript: String,
)