package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressUseCase
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPort
import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.CourseProgressRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CourseProgressService(
    private val courseProgressRepository: CourseProgressRepository,
    private val catalogPort: CatalogPort,
    private val courseProgressMapper: CourseProgressMapper
) : CourseProgressUseCase {

    @Transactional
    override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponse {

        val courseProgressId = CourseProgressId(userId, courseId)
        val firstLessonOfCourse = catalogPort.findFirstLessonIdInCourse(courseId)

        courseProgressRepository.upsert(userId, courseId, firstLessonOfCourse!!)

        val userCourseProgress = courseProgressRepository.findById(courseProgressId).orElseThrow()
        return courseProgressMapper.toCourseProgressResponse(userCourseProgress)

    }






}