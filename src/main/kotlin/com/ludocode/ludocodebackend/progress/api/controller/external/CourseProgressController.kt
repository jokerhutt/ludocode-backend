package com.ludocode.ludocodebackend.progress.api.controller.external

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROGRESS_COURSE)
class CourseProgressController {

    @GetMapping(PathConstants.USER_COURSE_PROGRESS)
    fun getCourseProgressList (@AuthenticationPrincipal(expression = "id") userId: UUID) {

    }

}