package com.ludocode.ludocodebackend.support.snapshot

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.lesson.app.service.LessonService
import com.ludocode.ludocodebackend.lesson.app.service.admin.LessonSnapshotService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class TestSnapshotService(
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val lessonService: LessonService,
    private val lessonSnapshotService: LessonSnapshotService,
    private val languagesMapper: LanguagesMapper
) {

    @Transactional
    fun buildCourseSnapshot(courseId: UUID): CourseSnap {

        val course = courseRepository.findById(courseId).orElseThrow { ApiException(ErrorCode.COURSE_NOT_FOUND) }
        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)
        val modules = moduleRepository
            .findAllByIdIn(moduleIds)
            .sortedBy { it.orderIndex }

        val moduleSnapshots = modules.map { module ->
            buildModuleSnapshot(module)
        }

        val codeLanguage = course.language?.let { lang -> languagesMapper.toLanguageMetadata(lang) }

        return CourseSnap(
            courseId,
            course.title,
            courseType = course.courseType,
            courseIcon = course.courseIcon,
            codeLanguage,
            moduleSnapshots
        )


    }

    private fun buildModuleSnapshot(module: Module): ModuleSnap {

        val moduleId = module.id

        val module = moduleRepository.findActiveById(moduleId)
        val lessons = moduleLessonsRepository.findActiveLessonsByModuleId(moduleId)

        val lessonSnapshots = lessons
            .map { lesson ->
                val orderIndex = moduleLessonsRepository.findOrderIndexForLesson(moduleId, lesson.id)
                orderIndex to lesson
            }
            .sortedBy { it.first }
            .map { (orderIndex, lesson) ->

                val exerciseResponses = lessonService.getExercisesByLessonId(lesson.id)

                val exerciseSnapshots = exerciseResponses.map {
                    lessonSnapshotService.buildExerciseSnapshot(it)
                }

                LessonSnap(
                    id = lesson.id!!,
                    title = lesson.title,
                    orderIndex = orderIndex,
                    exercises = exerciseSnapshots
                )
            }

        return ModuleSnap(
            moduleId = module!!.id,
            title = module.title,
            lessons = lessonSnapshots
        )
    }

}