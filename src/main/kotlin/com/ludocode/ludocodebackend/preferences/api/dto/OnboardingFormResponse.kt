package com.ludocode.ludocodebackend.preferences.api.dto

data class OnboardingFormResponse(
    val version: Int,
    val title: String,
    val fields: List<FormFieldDef>
)