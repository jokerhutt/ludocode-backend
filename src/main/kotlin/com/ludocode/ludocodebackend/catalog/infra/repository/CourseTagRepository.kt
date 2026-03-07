package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.CourseTag
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.CourseTagId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CourseTagRepository : JpaRepository<CourseTag, CourseTagId>{

    fun deleteByCourseTagIdCourseId(courseId: UUID)

}