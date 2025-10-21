package com.ludocode.ludocodebackend.user.infra.client

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICATALOG
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICOURSEPROGRESS
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IFIRST_LESSON_ID
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.user.app.port.out.CourseProgressPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class CourseProgressClient (
    private val rest: RestTemplate,
    @Value("\${course-progress.service.base-url}") private val courseServiceBaseUrl: String
): CourseProgressPort {

    override fun findOrCreate(userId: UUID, courseId: UUID): CourseProgressResponse {
        val url = "$courseServiceBaseUrl$ICOURSEPROGRESS/$courseId/$userId"
        val resp = rest.postForEntity(url, null, CourseProgressResponse::class.java)
        return resp.body ?: error("Could not find course progress")
    }

}