package com.ludocode.ludocodebackend.languages.api.dto

data class UpdateLanguageRequest(
    val name: String,
    val slug: String,
    val editorId: String,
    val pistonId: String,
    val extension: String,
    val runtimeVersion: String = "*",
    val base: String,
    val iconName: String,
    val initialScript: String
)