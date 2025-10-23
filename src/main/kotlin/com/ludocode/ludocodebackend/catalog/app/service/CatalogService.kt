package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.ExerciseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.FlatCourseTreeMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.LessonMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.ModuleMapper
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogUseCase
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.catalog.infra.projection.UserLessonProjection
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.nodes.NodeId
import java.util.UUID

@Service
class CatalogService(
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseMapper: ExerciseMapper,
    private val moduleMapper: ModuleMapper,
    private val lessonRepository: LessonRepository,
    private val lessonMapper: LessonMapper,
    private val flatCourseTreeMapper: FlatCourseTreeMapper
) : CatalogUseCase {

    override fun findFirstLessonIdInCourse(courseId: UUID): UUID? {
       return lessonRepository.findFirstLessonIdInCourse(courseId)
    }

    override fun findModuleIdForLesson(lessonId: UUID): UUID? {
       return lessonRepository.findModuleIdForLesson(lessonId)
    }

    override fun findNextLessonId(currentLesson: UUID): UUID? {
        return lessonRepository.findNextLessonId(currentLesson)
    }

    fun getAllCourses (): List<CourseResponse> {
        return courseMapper.toCourseResponseList(courseRepository.findAll())
    }

    fun getFlatCourseTree(courseId: UUID): FlatCourseTreeResponse {
        val rows = moduleRepository.findFlatCourseTree(courseId)
        return flatCourseTreeMapper.toFlatTree(courseId, rows)
    }

    fun getExercisesByLessonId (lessonId: UUID): List<ExerciseResponse> {
       val exercisesWithOptionsFlat: List<ExerciseFlatProjection> = exerciseRepository.getFlatExercisesWithOptions(lessonId)
       return exerciseMapper.toLessonExercises(exercisesWithOptionsFlat)
    }

    fun getModulesByCourseId (courseId: UUID): List<ModuleResponse> {
        val modules = moduleRepository.findAllByCourseId(courseId)
        return moduleMapper.toModuleResponseList(modules)
    }

    fun getLessonsByModuleId (moduleId: UUID, userId: UUID): List<LessonResponse> {
        val lessons: List<UserLessonProjection> = lessonRepository.findUserLessons(moduleId, userId)
        return lessonMapper.toLessonResponseList(lessons)
    }

    fun getModulesByIds (moduleIds: List<UUID>) : List<ModuleResponse> {
        val modules: List<Module> = moduleRepository.findAllByIdIn(moduleIds)
        return moduleMapper.toModuleResponseList(modules)
    }

    fun getLessonsByIds (lessonIds: List<UUID>, userId: UUID): List<LessonResponse> {
        val lessons: List<UserLessonProjection> = lessonRepository.findUserLessonsByIds(lessonIds, userId)
        return lessonMapper.toLessonResponseList(lessons)
    }

}