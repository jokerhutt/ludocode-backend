package com.ludocode.ludocodebackend.user.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.entity.User
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserMapper (private val basicMapper: BasicMapper){

    fun toUserResponse(user: User, hasOnboarded: Boolean): UserResponse =
        basicMapper.one(user) {
            UserResponse(
                id = it.id!!,
                displayName = it.displayName!!,
                avatarVersion = it.avatarVersion,
                avatarIndex = it.avatarIndex,
                email = it.email!!,
                createdAt = it.createdAt!!,
                hasOnboarded = hasOnboarded
            )
        }

    fun toUserResponseList(users: List<User>, onboardingMap: Map<UUID, Boolean>): List<UserResponse> =
        basicMapper.list(users) { user ->
            toUserResponse(user, onboardingMap[user.id] ?: false)
        }

}

