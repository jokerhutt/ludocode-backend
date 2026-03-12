package com.ludocode.ludocodebackend.catalog.app.service.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlLesson
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlModule
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlRoot
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.commons.configuration.web.YamlProperties
import com.ludocode.ludocodebackend.lesson.app.service.admin.LessonSnapshotService
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CurriculumYamlService(
    private val curriculumSnapshotService: CurriculumSnapshotService,
    private val lessonSnapshotService: LessonSnapshotService,
    private val courseRepository: CourseRepository,
    private val yamlProperties: YamlProperties,
) {


    private val yamlMapper =
        ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())

    @Transactional
    fun importYaml(courseId: UUID? = null, root: CurriculumYamlRoot) {

        val resolvedCourseId = courseId ?:
            curriculumSnapshotService.createCourse(CreateCourseRequest(
                courseTitle = root.title,
                requestHash = UUID.randomUUID(),
                description = root.description,
                courseType = root.courseType,
                courseIcon = root.courseIcon,
                languageId = root.languageId
            ))

        val lessonIdMap = mutableMapOf<CurriculumYamlLesson, UUID>()

        val snapshot = CurriculumDraftSnapshot(
            modules = root.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.id ?: UUID.randomUUID(),
                    title = module.title,
                    lessons = module.lessons.map { lesson ->

                        val lessonId = lesson.id ?: UUID.randomUUID()
                        lessonIdMap[lesson] = lessonId

                        LessonDraftSnapshot(
                            id = lessonId,
                            title = lesson.title,
                            lessonType = lesson.lessonType
                        )
                    }
                )
            }
        )

        curriculumSnapshotService.applyCurriculumDiffs(resolvedCourseId, snapshot)

        root.modules.forEach { module ->
            module.lessons.forEach { lesson ->
                val normalizedExercises = lesson.exercises.map { ex ->
                    ex.copy(
                        exerciseId = ex.exerciseId ?: UUID.randomUUID()
                    )
                }

                lessonSnapshotService.applyExercises(
                    lessonIdMap[lesson]!!,
                    LessonCurriculumDraftSnapshot(normalizedExercises, lessonType = lesson.lessonType)
                )
            }
        }
    }

    fun exportYaml(courseId: UUID): String {

        val course = courseRepository.findById(courseId)
            .orElseThrow()

        val curriculum = curriculumSnapshotService.buildCurriculumSnapshot(courseId)

        val modules = curriculum.modules.map { module ->

            val lessons = module.lessons.map { lesson ->

                val lessonSnap =
                    lessonSnapshotService.buildLessonCurriculumSnapshot(lesson.id)

                CurriculumYamlLesson(
                    id = lesson.id,
                    title = lesson.title,
                    exercises = lessonSnap.exercises,
                    lessonType = lessonSnap.lessonType
                )
            }

            CurriculumYamlModule(
                id = module.id,
                title = module.title,
                lessons = lessons
            )
        }

        val root = CurriculumYamlRoot(
            title = course.title,
            description = course.description,
            courseType = course.courseType,
            courseIcon = course.courseIcon,
            languageId = course.language?.id,
            modules = modules
        )

        val yaml = yamlMapper.writeValueAsString(root)
        val yamlSchemaServer = yamlProperties.curriculum

        return "# yaml-language-server: \$schema=$yamlSchemaServer/schemas/curriculum.schema.json\n\n$yaml"
    }

}