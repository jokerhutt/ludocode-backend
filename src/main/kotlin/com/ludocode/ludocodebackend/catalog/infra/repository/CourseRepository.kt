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
    left join fetch c.language l
    where c.isDeleted = false
    and c.isVisible = true
"""
    )
    fun findAllWithLanguage(): List<Course>

    fun findByTitle(title: String): Course?


}