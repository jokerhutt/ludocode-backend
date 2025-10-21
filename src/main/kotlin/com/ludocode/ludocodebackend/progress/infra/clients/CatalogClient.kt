package com.ludocode.ludocodebackend.progress.infra.clients

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICATALOG
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IFIRST_LESSON_ID
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class CatalogClient (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val catalogServiceBaseUrl: String
): CatalogPort {

    override fun findFirstLessonIdInCourse(courseId: UUID): UUID? {
        val url = "$catalogServiceBaseUrl$ICATALOG/$courseId/first"
        val resp = rest.getForEntity(url, UUID::class.java)
        return resp.body ?: error("Could not find first lesson")
    }


}