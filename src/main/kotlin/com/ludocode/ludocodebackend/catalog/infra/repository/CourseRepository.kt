package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
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
    and c.courseStatus != 'DRAFT'
"""
    )
    fun findAllWithLanguage(): List<Course>

    @Query(
        """
    select distinct c
    from Course c
    left join fetch c.language l
"""
    )
    fun findAllWithLanguagesIncludingDraft(): List<Course>

    fun countByCourseStatusAndIsDeletedFalse(courseStatus: CourseStatus): Long

    //TODO check this with unique stuff
    fun findByTitle(title: String): Course?

    fun existsByTitle(title: String): Boolean


}