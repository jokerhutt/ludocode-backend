package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseTreeMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.ExerciseMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.infra.projection.ModuleLessonProjection
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CatalogService(
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val courseTreeMapper: CourseTreeMapper,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseMapper: ExerciseMapper
) {

    fun getAllCourses (): List<CourseResponse> {
        return courseMapper.toCourseResponseList(courseRepository.findAll())
    }

    fun getCourseTree (userId: UUID, courseId: UUID): CourseTreeResponse {
        val course: Course = courseRepository.findById(courseId).orElseThrow()
        val modulesWithLessons: List<ModuleLessonProjection> = moduleRepository.findCourseTree(courseId, userId)
        return courseTreeMapper.toCourseTree(course, modulesWithLessons)
    }

    fun getExercisesByLessonId (lessonId: UUID): List<ExerciseResponse> {
        val exercises: List<Exercise> = exerciseRepository.findAllByLessonId(lessonId)
        return exerciseMapper.toExerciseResponseList(exercises)
    }

}