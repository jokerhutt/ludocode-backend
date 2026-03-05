package com.ludocode.ludocodebackend.preferences.api.dto

data class TogglePreferencesRequest(val value: Boolean, val key: PreferenceRequestKey)

enum class PreferenceRequestKey {AI, AUDIO}