package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressUseCase
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CourseProgressService(
    private val courseProgressRepository: CourseProgressRepository,
    private val catalogPortForProgress: CatalogPortForProgress,
    private val courseProgressMapper: CourseProgressMapper
) : CourseProgressUseCase {

    @Transactional
    override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponseWithEnrolled {
        val firstLessonOfCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.upsert(userId, courseId, firstLessonOfCourse!!)
        val userCourseProgress = courseProgressRepository.findProgressWithModule(userId, courseId)
        val enrolled = courseProgressRepository.findAllCourseIdsForUser(userId)
        return courseProgressMapper.toCourseProgressResponseWithEnrolled(userCourseProgress!!, enrolled)

    }

    fun getEnrolledCourseIds(userId: UUID) : List<UUID> {
        return courseProgressRepository.findAllCourseIdsForUser(userId)
    }

    fun findCourseProgressList(courseIds: List<UUID>, userId: UUID) : List<CourseProgressResponse> {
        return courseProgressMapper.toCourseProgressResponseList(courseProgressRepository.findAllProgressWithModulesByUserAndCourses(userId, courseIds))
    }

    fun findCourseProgress(userId: UUID, courseId: UUID): CourseProgressResponse {
        return courseProgressMapper.toCourseProgressResponse(courseProgressRepository.findProgressWithModule(userId, courseId) ?: throw IllegalStateException("progress not found"))
    }

    @Transactional
    fun updateLesson(userId: UUID, courseId: UUID, newLessonId: UUID) : CourseProgressResponse {
        courseProgressRepository.setCurrentLesson(userId = userId, courseId = courseId, newLessonId = newLessonId)
        return findCourseProgress(userId, courseId)
    }


}