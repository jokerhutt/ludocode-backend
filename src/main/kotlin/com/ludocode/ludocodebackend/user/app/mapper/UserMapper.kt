package com.ludocode.ludocodebackend.user.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper (private val basicMapper: BasicMapper){

    fun toUserResponse(user: User): UserResponse =
        basicMapper.one(user) {
            UserResponse(
                id = it.id!!,
                firstName = it.firstName!!,
                lastName = it.lastName!!,
                pfpSrc = it.pfpSrc!!,
                email = it.email!!,
                createdAt = it.createdAt!!,
                currentCourse = it.currentCourse
            )
        }

    fun toUserResponseList(users: List<User>): List<UserResponse> =
        basicMapper.list(users) {user ->
            toUserResponse(user)
        }

}