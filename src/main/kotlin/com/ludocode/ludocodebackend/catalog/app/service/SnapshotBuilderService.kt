package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectSnap
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForAI
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SnapshotBuilderService(
    private val moduleRepository: ModuleRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val catalogService: CatalogService,
    private val courseRepository: CourseRepository,
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val languagesMapper: LanguagesMapper,
) : CatalogPortForAI {

    private val logger = LoggerFactory.getLogger(SnapshotBuilderService::class.java)

    override fun findExerciseSnapshotById(exerciseId: UUID): ExerciseSnap {
        val exerciseResponse = catalogService.getExerciseByExerciseId(exerciseId)
        return buildExerciseSnapshot(exerciseResponse)
    }

    @Transactional
    fun buildCourseSnapshot (courseId: UUID): CourseSnap {

        val course = courseRepository.findById(courseId).orElseThrow{ ApiException(ErrorCode.COURSE_NOT_FOUND) }
        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)
        val modules = moduleRepository.findAllByIdIn(moduleIds)

        logger.info(
            LogEvents.COURSE_SNAPSHOT_BUILT + " {} {}",
            kv(LogFields.COURSE_ID, courseId.toString()),
            kv(LogFields.MODULE_COUNT, modules.size),
        )

        val moduleSnapshots = modules.map { module ->
            buildModuleSnapshot(module)
        }

        val subject = course.subject

        val codeLanguage = course.language?.let { lang -> languagesMapper.toLanguageMetadata(lang) }


        val subjectSnap = SubjectSnap(slug = subject.slug, name = subject.name)

        return CourseSnap(courseId, course.title, courseType = course.courseType, courseSubject = subjectSnap, codeLanguage,moduleSnapshots)


    }


    fun buildCurriculumSnapshot (courseId: UUID) : CurriculumDraftSnapshot {

        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)
        val modules = moduleRepository.findAllByIdIn(moduleIds)

        val moduleDraftSnapshots = modules.map { module ->
            buildModuleDraftSnapshot(module)
        }

        return CurriculumDraftSnapshot(modules = moduleDraftSnapshots)

    }

    private fun buildModuleDraftSnapshot (module: Module) : ModuleDraftSnapshot {

        val moduleId = module.id
        val lessons = moduleLessonsRepository.findActiveLessonsByModuleId(moduleId)

        val lessonDraftSnapshots = lessons.map { lesson ->
            LessonDraftSnapshot(
                id = lesson.id,
                title = lesson.title,
            )
        }

        return ModuleDraftSnapshot(id=moduleId, title = module.title, lessons = lessonDraftSnapshots)
    }

    private fun buildModuleSnapshot (module: Module) : ModuleSnap {

        val moduleId = module.id

        val module = moduleRepository.findActiveById(moduleId)
        val lessons = moduleLessonsRepository.findActiveLessonsByModuleId(moduleId)

        val lessonSnapshots = lessons.map { lesson ->
            val exerciseResponses = catalogService.getExercisesByLessonId(lesson.id)

            val exerciseSnapshots = exerciseResponses.map { exerciseResponse ->
                buildExerciseSnapshot(exerciseResponse)
            }

            val orderIndex = moduleLessonsRepository.findOrderIndexForLesson(moduleId, lesson.id)

            LessonSnap(
                id = lesson.id!!,
                title = lesson.title,
                orderIndex = orderIndex,
                exercises = exerciseSnapshots
            )
        }

        return ModuleSnap (
            moduleId = module!!.id,
            title = module.title,
            lessons = lessonSnapshots
        )
    }



    internal fun buildExerciseSnapshot (exerciseResponse: ExerciseResponse) : ExerciseSnap {
       return ExerciseSnap(
            id = exerciseResponse.id,
            title = exerciseResponse.title,
            subtitle = exerciseResponse.subtitle,
            prompt = exerciseResponse.prompt,
            exerciseType = exerciseResponse.exerciseType,
            media = exerciseResponse.exerciseMedia,
            correctOptions = exerciseResponse.correctOptions.map { opt ->
                OptionSnap(
                    content = opt.content,
                    answerOrder = opt.answerOrder,
                    exerciseOptionId = (opt.id)
                )
            },
            distractors = exerciseResponse.distractors.map { opt ->
                OptionSnap(
                    content = opt.content,
                    answerOrder = opt.answerOrder,
                    exerciseOptionId = opt.id
                )
            }
        )
    }


}