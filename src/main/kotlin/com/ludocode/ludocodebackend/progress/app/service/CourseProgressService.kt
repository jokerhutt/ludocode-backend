package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.progress.api.dto.internal.CourseProgressWithCompletion
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressUseCase
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CourseProgressService(
    private val courseProgressRepository: CourseProgressRepository,
    private val catalogPortForProgress: CatalogPortForProgress,
    private val courseProgressMapper: CourseProgressMapper,
    private val clock: Clock,
    private val lessonCompletionRepository: LessonCompletionRepository,
) : CourseProgressUseCase {

    @Transactional
     override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponseWithEnrolled {
        val firstLessonOfCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.upsert(userId, courseId, firstLessonOfCourse!!, OffsetDateTime.now(clock))
        val userCourseProgress = courseProgressRepository.findProgressWithModule(userId, courseId)
        val enrolled = courseProgressRepository.findAllCourseIdsForUser(userId)
        return courseProgressMapper.toCourseProgressResponseWithEnrolled(userCourseProgress!!, enrolled)
    }


    @Transactional
    fun resetUserCourseProgress(userId: UUID, courseId: UUID) : CourseProgressResponse {
        lessonCompletionRepository.deleteLessonCompletionsForUserAndCourse(userId, courseId)
        val firstLessonIdInCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.resetCourseProgressForUser(userId, courseId, firstLessonIdInCourse)
        return findCourseProgress(userId, courseId)
    }

    fun getEnrolledCourseIds(userId: UUID) : List<UUID> {
        return courseProgressRepository.findAllCourseIdsForUser(userId)
    }

    fun findCurrentCourseId(userId: UUID) : UUID? {
        return courseProgressRepository.findCurrentCourseIdForUser(userId)
    }

    fun findCourseProgressList(courseIds: List<UUID>, userId: UUID) : List<CourseProgressResponse> {
        return courseProgressMapper.toCourseProgressResponseList(courseProgressRepository.findAllProgressWithModulesByUserAndCourses(userId, courseIds))
    }

    fun findCourseProgress(userId: UUID, courseId: UUID): CourseProgressResponse {
        return courseProgressMapper.toCourseProgressResponse(courseProgressRepository.findProgressWithModule(userId, courseId) ?: throw IllegalStateException("progress not found"))
    }

    @Transactional
    fun updateLesson(userId: UUID, courseId: UUID, isCompleted: Boolean, newLessonId: UUID?) : CourseProgressWithCompletion? {
        if (isCompleted) return CourseProgressWithCompletion(findCourseProgress(userId, courseId), false)

        var isFirstCompletion = false
        if (newLessonId != null) {
            courseProgressRepository.setCurrentLesson(userId = userId, courseId = courseId, newLessonId = newLessonId,
                OffsetDateTime.now(clock))
        } else {
            val courseProgress = courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow()
            if (!courseProgress.isComplete) {
                courseProgressRepository.markCourseComplete(userId, courseId)
                isFirstCompletion = true
            }
        }
        return CourseProgressWithCompletion(findCourseProgress(userId, courseId), isFirstCompletion)
    }




}