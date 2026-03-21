package com.ludocode.ludocodebackend.languages.api.dto

data class LanguageToggleRequest (
    val enabled: Boolean,
    val message: String? = null
)