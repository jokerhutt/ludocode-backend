package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface LessonCompletionRepository : JpaRepository<LessonCompletion, UUID> {


    @Modifying
    @Query(""" 
        update lesson_completion
        set is_deleted = true
        where user_id = :userId
        and course_id = :courseId
    """, nativeQuery = true)
    fun deleteLessonCompletionsForUserAndCourse(userId: UUID, courseId: UUID)

    fun existsByIdAndIsDeletedFalse(id: UUID): Boolean

    fun existsByUserIdAndLessonIdAndIsDeletedFalse(userId: UUID, lessonId: UUID): Boolean

}