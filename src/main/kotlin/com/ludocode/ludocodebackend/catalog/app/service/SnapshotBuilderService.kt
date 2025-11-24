package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.app.port.`in`.SnapshotUseCase
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseOptionRepository
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
    private val catalogService: CatalogService,
    private val exerciseOptionRepository: ExerciseOptionRepository
) : SnapshotUseCase {


    override fun findExerciseSnapshotById(exerciseId: UUID): ExerciseSnap {
        val exerciseResponse = catalogService.getExerciseByExerciseId(exerciseId)
        return buildExerciseSnapshot(exerciseResponse)
    }

    fun buildCourseSnapshot (courseId: UUID): CourseSnap {

        val moduleIds = moduleRepository.findActiveIdsByCourse(courseId)
        val modules = moduleRepository.findAllByIdIn(moduleIds)

        val moduleSnapshots = modules.map { module ->
            buildModuleSnapshot(module)
        }

        return CourseSnap(courseId, title = "", moduleSnapshots)


    }

    private fun buildModuleSnapshot (module: Module) : ModuleSnapshot {

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

        return ModuleSnapshot (
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