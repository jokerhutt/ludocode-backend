package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.FlatCourseTreeMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.ModuleMapper
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.catalog.domain.entity.CourseTag
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.CourseTagId
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseTagRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.tag.infra.repository.TagRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.*

@Service
class CatalogService(
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val moduleMapper: ModuleMapper,
    private val lessonRepository: LessonRepository,
    private val flatCourseTreeMapper: FlatCourseTreeMapper,
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val tagRepository: TagRepository,
    private val courseTagRepository: CourseTagRepository,
) : CatalogPortForProgress {

    private val logger = LoggerFactory.getLogger(CatalogService::class.java)


    @Cacheable(CacheNames.COURSE_FIRST_MODULE, key = "#courseId")
    override fun findFirstModuleIdInCourse(courseId: UUID): UUID {
        return lessonRepository.findFirstModuleIdInCourse(courseId) ?: throw ApiException(ErrorCode.LESSON_NOT_FOUND)
    }

    @Cacheable(CacheNames.LESSON_MODULE, key = "#lessonId")
    override fun findModuleIdForLesson(lessonId: UUID): UUID {
        return lessonRepository.findModuleIdForLesson(lessonId)
            ?: throw ApiException(ErrorCode.MODULE_NOT_FOUND_FOR_LESSON)
    }

    @Cacheable(CacheNames.COURSE_LIST)
    fun getAllCourses(): List<CourseResponse> {
        return courseMapper.toCourseResponseList(courseRepository.findAllWithLanguage())
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

    internal fun getModulesByIds(moduleIds: List<UUID>): List<ModuleResponse> {
        val modules: List<Module> = moduleRepository.findAllByIdIn(moduleIds)
        return moduleMapper.toModuleResponseList(modules)
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    fun changeCourseTags(courseId: UUID, tagIds: List<Long>) {

        val course = courseRepository.findById(courseId)
            .orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }

        val tags = tagRepository.findByIdIn(tagIds)

        if (tagIds.size != tagIds.toSet().size) {
            throw ApiException(ErrorCode.DUPLICATE_TAGS)
        }

        courseTagRepository.deleteByCourseTagIdCourseId(courseId)

        val newTags = tags.map {
            CourseTag(
                CourseTagId(courseId, it.id)
            )
        }

        courseTagRepository.saveAll(newTags)
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_TREE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_FIRST_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_MODULE], allEntries = true),
            CacheEvict(cacheNames = [CacheNames.LESSON_EXERCISES], allEntries = true)
        ]
    )
    @Transactional
    fun updateCourseLanguage(courseId: UUID, languageId: Long) {
        val currentCourse = courseRepository.findById(courseId).orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }
        val chosenLanguage = codeLanguagesRepository.findById(languageId).orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }

        if (currentCourse.language?.id == chosenLanguage.id) {
            return
        }

        currentCourse.language = chosenLanguage
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [CacheNames.COURSE_LIST], allEntries = true),
        ]
    )
    @Transactional
    fun updateCourseIcon(courseId: UUID, iconName: String) {
        val course = courseRepository.findById(courseId).orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }
        course.courseIcon = iconName
    }




}