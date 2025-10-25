package com.ludocode.ludocodebackend.auth.api.dto.response

import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse

data class UserLoginResponse(val user: UserResponse, val userStats: UserStatsResponse)
