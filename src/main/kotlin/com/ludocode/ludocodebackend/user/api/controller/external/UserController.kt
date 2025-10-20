package com.ludocode.ludocodebackend.user.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.user.api.dto.request.ChangeCourseRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.USERS)
class UserController(private val userService: UserService) {

    @GetMapping(PathConstants.USERS_IDS)
    fun getUsersByIds(@RequestParam userIds: List<UUID>) : ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getUsersByIds(userIds))
    }

    @PatchMapping(PathConstants.UPDATE_COURSE)
    fun updateCurrentCourse(
        @RequestBody request: ChangeCourseRequest, @AuthenticationPrincipal(expression = "id") userId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.updateCourse(userId, request.newCourseId))
    }




}