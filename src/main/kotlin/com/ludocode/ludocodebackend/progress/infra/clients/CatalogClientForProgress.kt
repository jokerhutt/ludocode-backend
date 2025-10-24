package com.ludocode.ludocodebackend.progress.infra.clients

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICATALOG
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class CatalogClientForProgress (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val catalogServiceBaseUrl: String
): CatalogPortForProgress {

    override fun findFirstLessonIdInCourse(courseId: UUID): UUID? {
        val url = "$catalogServiceBaseUrl$ICATALOG/$courseId/first"
        System.out.println("URL IS: " + url)
        val resp = rest.getForEntity(url, UUID::class.java)
        return resp.body ?: error("Could not find first lesson")
    }

    override fun findModuleIdForLesson(lessonId: UUID): UUID? {
        val url = "$catalogServiceBaseUrl$ICATALOG/$lessonId/module"
        val resp = rest.getForEntity(url, UUID::class.java)
        return resp.body ?: error("Could not find Module ID")
    }


    override fun findLessonIdTree(lessonId: UUID): LessonTreeWithIdDTO? {
        val url = "$catalogServiceBaseUrl$ICATALOG/$lessonId/tree"
        val resp = rest.getForEntity(url, LessonTreeWithIdDTO::class.java)
        return resp.body ?: error("Could not get lesson id tree")
    }

    override fun findCourseIdForLesson(lessonId: UUID): UUID? {
        val url = "$catalogServiceBaseUrl$ICATALOG/$lessonId/course"
        val resp = rest.getForEntity(url, UUID::class.java)
        return resp.body ?: error("Could not find Course ID")
    }

    override fun findNextLessonId(lessonId: UUID): UUID? {
       val url = "$catalogServiceBaseUrl$ICATALOG/$lessonId/next"
       val resp = rest.getForEntity(url, UUID::class.java)
       return resp.body ?: error("Could not find next lesson")
    }

}