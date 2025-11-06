package com.ludocode.ludocodebackend.user.infra.client

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICOURSEPROGRESS
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICOURSEPROGRESSFINDCREATE
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.user.app.port.out.CourseProgressPortForUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class CourseProgressClientForUser (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val progressServiceBaseUrl: String
): CourseProgressPortForUser {

    override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponseWithEnrolled  {
        val url = "$progressServiceBaseUrl$ICOURSEPROGRESS/$courseId/$userId"
        val resp = rest.postForEntity(url, null, CourseProgressResponseWithEnrolled::class.java)
        return resp.body ?: error("User service returned empty body")
    }

}