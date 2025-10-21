package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressUseCase
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
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
    override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponse {
        val firstLessonOfCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.upsert(userId, courseId, firstLessonOfCourse!!)
        val userCourseProgress = courseProgressRepository.findProgressWithModule(userId, courseId)
        return courseProgressMapper.toCourseProgressResponse(userCourseProgress!!)

    }

    fun findCourseProgressList(userId: UUID) : List<CourseProgressResponse> {
        return courseProgressMapper.toCourseProgressResponseList(courseProgressRepository.findAllProgressWithModulesByUser(userId))
    }






}