package com.ludocode.ludocodebackend.catalog.app.service
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.lesson.app.mapper.ExerciseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.FlatCourseTreeMapper
import com.ludocode.ludocodebackend.lesson.app.mapper.LessonMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.ModuleMapper
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.lesson.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.lesson.infra.repository.UserLessonProjection
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonExercisesRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonRepository
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
    private val moduleMapper: ModuleMapper,
    private val lessonRepository: LessonRepository,
    private val flatCourseTreeMapper: FlatCourseTreeMapper,
) : CatalogPortForProgress {

    private val logger = LoggerFactory.getLogger(CatalogService::class.java)


    @Cacheable(CacheNames.COURSE_FIRST_MODULE, key = "#courseId")
    override fun findFirstModuleIdInCourse(courseId: UUID): UUID {
        return lessonRepository.findFirstModuleIdInCourse(courseId) ?: throw ApiException(ErrorCode.LESSON_NOT_FOUND)
    }

    @Cacheable(CacheNames.LESSON_MODULE, key = "#lessonId")
    override fun findModuleIdForLesson(lessonId: UUID): UUID {
       return lessonRepository.findModuleIdForLesson(lessonId) ?: throw ApiException(ErrorCode.MODULE_NOT_FOUND_FOR_LESSON)
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

    internal fun getModulesByIds (moduleIds: List<UUID>) : List<ModuleResponse> {
        val modules: List<Module> = moduleRepository.findAllByIdIn(moduleIds)
        return moduleMapper.toModuleResponseList(modules)
    }


}