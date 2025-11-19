package com.ludocode.ludocodebackend.auth.api.dto.response

import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse

data class UserLoginResponse(val user: UserResponse, val userStats: UserCoinsResponse, val userStreak: UserStreakResponse)
