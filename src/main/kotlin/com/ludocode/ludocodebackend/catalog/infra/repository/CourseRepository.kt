package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CourseRepository : JpaRepository<Course, UUID> {

    @Query(
        """
    select distinct c
    from Course c
    join fetch c.subject s
    left join fetch c.language l
"""
    )
    fun findAllWithSubjectAndLanguage(): List<Course>

    fun existsBySubjectId(subjectId: Long): Boolean

    fun findByTitle(title: String): Course?


}