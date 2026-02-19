package com.ludocode.ludocodebackend.user.api.dto.request

import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo

data class EditProfileRequest(
    val username: String,
    val avatarInfo: AvatarInfo
)