package com.ludocode.ludocodebackend.projects.api.dto.request

import com.ludocode.ludocodebackend.projects.domain.enums.Visibility

data class ChangeVisibilityRequest (
    val value: Visibility
)