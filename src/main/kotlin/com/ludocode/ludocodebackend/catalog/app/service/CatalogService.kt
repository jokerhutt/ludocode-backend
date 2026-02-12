package com.ludocode.ludocodebackend.catalog.app.service
import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
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
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.catalog.infra.projection.UserLessonProjection
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CatalogService(
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val exerciseMapper: ExerciseMapper,
    private val moduleMapper: ModuleMapper,
    private val lessonRepository: LessonRepository,
    private val lessonMapper: LessonMapper,
    private val flatCourseTreeMapper: FlatCourseTreeMapper,
    private val lessonExercisesRepository: LessonExercisesRepository,
) : CatalogPortForProgress {

    private val logger = LoggerFactory.getLogger(CatalogService::class.java)


    @Cacheable(CacheNames.COURSE_FIRST_LESSON, key = "#courseId")
    override fun findFirstLessonIdInCourse(courseId: UUID): UUID {
       return lessonRepository.findFirstLessonIdInCourse(courseId) ?: throw ApiException(ErrorCode.LESSON_NOT_FOUND)
    }

    @Cacheable(CacheNames.COURSE_FIRST_MODULE, key = "#courseId")
    override fun findFirstModuleIdInCourse(courseId: UUID): UUID {
        return lessonRepository.findFirstModuleIdInCourse(courseId) ?: throw ApiException(ErrorCode.LESSON_NOT_FOUND)
    }

    @Cacheable(CacheNames.LESSON_MODULE, key = "#lessonId")
    override fun findModuleIdForLesson(lessonId: UUID): UUID {
       return lessonRepository.findModuleIdForLesson(lessonId) ?: throw ApiException(ErrorCode.MODULE_NOT_FOUND_FOR_LESSON)
    }

    @Cacheable(CacheNames.LESSON_NEXT, key = "#currentLesson")
    override fun findNextLessonId(currentLesson: UUID): UUID? {
        return lessonRepository.findNextLessonId(currentLesson)
    }

    @Cacheable(CacheNames.LESSON_COURSE, key = "#lessonId")
    override fun findCourseIdForLesson(lessonId: UUID): UUID {
       return lessonRepository.findCourseIdByLesson(lessonId) ?: throw ApiException(ErrorCode.COURSE_NOT_FOUND)
    }

    @Cacheable(CacheNames.LESSON_TREE, key = "#lessonId")
    override fun findLessonIdTree(lessonId: UUID) : LessonTreeWithIdDTO {
       val raw = lessonRepository.findLessonIdTree(lessonId) ?: throw ApiException(ErrorCode.TREE_NOT_FOUND)
        return LessonTreeWithIdDTO(raw.lessonId, raw.moduleId, raw.courseId, raw.nextLessonId)
    }

    override fun findLessonResponseById(lessonId: UUID, userId: UUID): LessonResponse {
        return lessonMapper.toLessonResponse(lessonRepository.findUserLesson(lessonId, userId) ?: throw ApiException(
            ErrorCode.LESSON_NOT_FOUND))
    }

    @Cacheable(CacheNames.COURSE_LIST)
    fun getAllCourses (): List<CourseResponse> {
        return courseMapper.toCourseResponseList(courseRepository.findAllWithSubjectAndLanguage())
    }

    @Cacheable(CacheNames.COURSE_TREE, key = "#courseId")
    fun getFlatCourseTree(courseId: UUID): FlatCourseTreeResponse {
        val rows = moduleRepository.findFlatCourseTree(courseId)
        logger.info(
            LogEvents.COURSE_TREE_LOADED,
            kv(LogFields.MODULE_COUNT, rows.size)
        )
        return flatCourseTreeMapper.toFlatTree(courseId, rows)
    }

    @Cacheable(CacheNames.LESSON_EXERCISES, key = "#lessonId")
    fun getExercisesByLessonId (lessonId: UUID): List<ExerciseResponse> {
       val exercisesWithOptionsFlat: List<ExerciseFlatProjection> = lessonExercisesRepository.getFlatExercisesWithOptions(lessonId)
        logger.info(
            LogEvents.LESSON_EXERCISES_LOADED + " {}",
            kv(LogFields.EXERCISE_COUNT, exercisesWithOptionsFlat.size)
        )
       return exerciseMapper.toLessonExercises(exercisesWithOptionsFlat)
    }

    @Cacheable(CacheNames.EXERCISE_SINGLE, key = "#exerciseId")
    fun getExerciseByExerciseId (exerciseId: UUID) : ExerciseResponse {
        val exerciseWithOptions = lessonExercisesRepository.getSingleExerciseNewestFlat(exerciseId)
        return exerciseMapper.toExerciseResponse(exerciseWithOptions)
    }

    internal fun getModulesByIds (moduleIds: List<UUID>) : List<ModuleResponse> {
        val modules: List<Module> = moduleRepository.findAllByIdIn(moduleIds)
        return moduleMapper.toModuleResponseList(modules)
    }

    internal fun getLessonsByIds (lessonIds: List<UUID>, userId: UUID): List<LessonResponse> {
        val lessons: List<UserLessonProjection> = lessonRepository.findUserLessonsByIds(lessonIds, userId)
        return lessonMapper.toLessonResponseList(lessons)
    }

}