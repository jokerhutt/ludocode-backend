package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SnapshotBuilderService(
    private val moduleRepository: ModuleRepository,
    private val lessonRepository: LessonRepository,
    private val moduleLessonsRepository: ModuleLessonsRepository,
    private val catalogService: CatalogService
) {

    fun buildCourseSnapshot (courseId: UUID) {

        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)

        val moduleSnapshots = moduleIds.map { moduleId ->
            buildModuleSnapshot()
        }


    }

    fun buildModuleSnapshot (module: Module) : ModuleSnapshot {

        val moduleId = module.id

        val module = moduleRepository.findActiveById(moduleId)
        val lessons = moduleLessonsRepository.findActiveLessonsByModuleId(moduleId)

        val lessonSnapshots = lessons.map { lesson ->
            val exerciseResponses = catalogService.getExercisesByLessonId(lesson.id)

            val exerciseSnapshots = exerciseResponses.map { exerciseResponse ->
                ExerciseSnap(
                    id = exerciseResponse.id,
                    title = exerciseResponse.title,
                    subtitle = exerciseResponse.subtitle,
                    prompt = exerciseResponse.prompt!!,
                    exerciseType = exerciseResponse.exerciseType,
                    correctOptions = exerciseResponse.correctOptions.map { opt ->
                        OptionSnap(
                            content = opt.content,
                            answerOrder = opt.answerOrder
                        )
                    },
                    distractors = exerciseResponse.distractors.map { opt ->
                        OptionSnap(
                            content = opt.content,
                            answerOrder = opt.answerOrder
                        )
                    }
                )
            }

            val orderIndex = moduleLessonsRepository.findOrderIndexForLesson(moduleId, lesson.id)

            LessonSnap(
                id = lesson.id!!,
                title = lesson.title,
                orderIndex = orderIndex,
                exercises = exerciseSnapshots
            )
        }

        return ModuleSnapshot (
            moduleId = module!!.id,
            title = module.title,
            lessons = lessonSnapshots
        )
    }


}