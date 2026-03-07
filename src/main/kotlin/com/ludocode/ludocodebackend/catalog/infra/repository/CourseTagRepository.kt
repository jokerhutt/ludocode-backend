package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTagMetadata
import com.ludocode.ludocodebackend.catalog.domain.entity.CourseTag
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.CourseTagId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CourseTagRepository : JpaRepository<CourseTag, CourseTagId>{

    fun deleteByCourseTagIdCourseId(courseId: UUID)

    @Query("""
    select new com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTagMetadata(
        ct.courseTagId.courseId,
        t.id,
        t.name,
        t.slug
    )
    from CourseTag ct
    join Tag t on t.id = ct.courseTagId.tagId
    """)
    fun getAllCourseTags(): List<CourseTagMetadata>

}