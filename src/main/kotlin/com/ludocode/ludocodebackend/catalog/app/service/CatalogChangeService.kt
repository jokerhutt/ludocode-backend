package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseOptionDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.LessonDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ModuleDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCLessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseOptionRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class CatalogChangeService(
    private val lessonRepository: LessonRepository,
    private val moduleRepository: ModuleRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val catalogService: CatalogService
) {


    @Transactional
    fun applyLessonDif (req: ModuleDiffRequest): CCModuleResponse {

        //1 lock scope
        val moduleOptional = moduleRepository.findByIdForUpdate(req.moduleId)
        if (moduleOptional.isEmpty) throw IllegalStateException("Module id error")
        val module = moduleOptional.get()
        //2 validate request

        //3 update module
        module.title = req.title

        //4 soft delete lessons
        if (!req.lessonsToDelete.isEmpty()) {
            lessonRepository.softDeleteIn(req.lessonsToDelete)
        }

        //5 upsert changes
        for (lessonDiff: LessonDiffRequest in req.changedLessons) {
            upsertLessonDiff(lessonDiff, module.id!!)
        }

        if (req.orderByIds != null) {
            val ids = req.orderByIds
            require(ids.distinct().size == ids.size) { "Duplicate lesson ids in order" }

            val lessons = lessonRepository.findAllById(ids)
            require(lessons.size == ids.size) { "Unknown lesson ids in order" }
            require(lessons.all { it.moduleId == req.moduleId }) { "Lesson not in module" }
            require(lessons.none { it.isDeleted == true }) { "Deleted lesson in ordering" }

            ids.forEach { lessonRepository.bumpOrderTemp(it) }
            ids.forEachIndexed { idx, id -> lessonRepository.setOrder(id, idx) }
        }


        val newModule = moduleRepository.findById(module.id!!).orElseThrow()

        var lessonsResponseList = mutableListOf<CCLessonResponse>()

        val newLessons = lessonRepository.findAllByModuleId(module.id!!)

        for (lesson: Lesson in newLessons) {
            val newExercises = catalogService.getExercisesByLessonId(lesson.id!!)
            lessonsResponseList.add(CCLessonResponse(lesson, newExercises))
        }

        return CCModuleResponse(module = newModule, lessons = lessonsResponseList)

    }





    fun upsertLessonDiff(lessonDiff: LessonDiffRequest, moduleId: UUID) {
        val lessonId = if (lessonDiff.lessonId == null) {
            // create new lesson
            val newLesson = Lesson(
                id = UUID.randomUUID(),
                moduleId = moduleId,
                title = lessonDiff.title,
                isDeleted = false
            )
            lessonRepository.save(newLesson)
            newLesson.id!!  // ✅ capture real lessonId here
        } else {
            // update existing
            val lesson = lessonRepository.findById(lessonDiff.lessonId).orElseThrow()
            require(lesson.moduleId == moduleId) { "Lesson not in module" }
            if (lesson.isDeleted == true) error("Lesson is deleted")
            lesson.title = lessonDiff.title
            lessonRepository.save(lesson)
            lesson.id!!   // ✅ existing id
        }


        // 1. Delete exercises
        if (lessonDiff.exercisesToDelete.isNotEmpty()) {
            insertDeletedVersions(lessonId, lessonDiff.exercisesToDelete)
        }

        // 2. Changed or new exercises
        for (exerciseDiff in lessonDiff.changedExercises) {
            val (exerciseId, version) = upsertExerciseVersion(lessonId, exerciseDiff)
            replaceOptions(exerciseId, version, exerciseDiff.options)
        }



    }

    private fun upsertExerciseVersion(
        lessonId: UUID,
        ed: ExerciseDiffRequest
    ): Pair<UUID, Int> {

        val exerciseId = ed.id ?: UUID.randomUUID()
        val version =
            if (ed.id == null) 1
            else exerciseRepository.bumpVersion(exerciseId)

        val row = Exercise(
            exerciseId = ExerciseId(exerciseId, version),
            title = ed.title,
            prompt = ed.prompt,
            exerciseType = ed.exerciseType,
            lessonId = lessonId,
            isDeleted = false
        )

        exerciseRepository.save(row)
        return exerciseId to version
    }

    private fun replaceOptions(
        exerciseId: UUID,
        version: Int,
        options: List<ExerciseOptionDiffRequest>
    ) {
        exerciseOptionRepository.deleteByExerciseIdAndVersion(exerciseId, version)

        val rows = options.mapIndexed { i, o ->
            ExerciseOption(
                id = o.id ?: UUID.randomUUID(),
                content = o.content,
                answerOrder = o.answerOrder ?: i + 1,
                exerciseId = exerciseId,
                exerciseVersion = version
            )
        }
        exerciseOptionRepository.saveAll(rows)
    }

    fun insertDeletedVersions(lessonId: UUID, exerciseIds: List<UUID>) {
        exerciseIds.forEach { exerciseId ->
            val nextVersion = exerciseRepository.bumpVersion(exerciseId)
            exerciseRepository.save(
                Exercise(
                    exerciseId = ExerciseId(exerciseId, nextVersion),
                    title = "",
                    prompt = "",
                    exerciseType = ExerciseType.CLOZE,
                    lessonId = lessonId,
                    isDeleted = true
                )
            )
        }
    }





}