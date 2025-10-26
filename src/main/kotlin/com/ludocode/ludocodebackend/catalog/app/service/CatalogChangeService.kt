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
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
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
    private val catalogService: CatalogService,
    @PersistenceContext private val entityManager: EntityManager
) {

    @Transactional
    fun applyLessonDif(req: ModuleDiffRequest): CCModuleResponse {
        val module = moduleRepository.findByIdForUpdate(req.moduleId).orElseThrow()
        module.title = req.title
        moduleRepository.save(module)

        if (req.lessonsToDelete.isNotEmpty()) lessonRepository.softDeleteIn(req.lessonsToDelete)

        val tempToReal = mutableMapOf<UUID, UUID>()
        req.changedLessons.forEach { ld ->
            tempToReal[ld.tempId] = ld.lessonId ?: UUID.randomUUID()
        }

        req.changedLessons.forEach { ld ->
            upsertLesson(ld, module.id!!, tempToReal[ld.tempId]!!)
        }

        req.orderByIds?.let { orderKeys ->
            val finalOrder = orderKeys.map { tempToReal[it] ?: it }

            val active = lessonRepository.findActiveIdsByModule(module.id!!)
            require(finalOrder.size == active.size) { "Order must include all active lessons" }
            require(finalOrder.toSet() == active.toSet()) { "Order IDs mismatch active lessons" }

            entityManager.flush(); entityManager.clear()
            lessonRepository.bumpAllInModule(module.id!!)
            finalOrder.forEachIndexed { idx, id -> lessonRepository.setOrder(id, idx + 1) }
            entityManager.flush(); entityManager.clear()
        }

        val newModule = moduleRepository.findById(module.id!!).orElseThrow()
        val lessons = lessonRepository.findAllByModuleId(module.id!!)
        val responses = lessons.map { l -> CCLessonResponse(l, catalogService.getExercisesByLessonId(l.id!!)) }
        return CCModuleResponse(newModule, responses)
    }

    private fun upsertLesson(ld: LessonDiffRequest, moduleId: UUID, realId: UUID) {
        val managed = lessonRepository.findById(realId).orElse(null)

        if (managed == null) {
            val newLesson = Lesson(
                id = realId,
                moduleId = moduleId,
                title = ld.title,
                isDeleted = false
            )
            entityManager.persist(newLesson)
        } else {
            require(!managed.isDeleted) { "Lesson is deleted" }
            require(managed.moduleId == moduleId) { "Lesson not in module" }
            managed.title = ld.title
        }

        if (ld.exercisesToDelete.isNotEmpty()) insertDeletedVersions(realId, ld.exercisesToDelete)

        ld.changedExercises.forEach { ed ->
            val (exerciseId, version) = upsertExerciseVersion(realId, ed)
            replaceOptions(exerciseId, version, ed.options)
        }
    }

    private fun upsertExerciseVersion(
        lessonId: UUID,
        ed: ExerciseDiffRequest
    ): Pair<UUID, Int> {
        val exerciseId = ed.id ?: UUID.randomUUID()
        val version = if (ed.id == null) 1 else exerciseRepository.bumpVersion(exerciseId)

        exerciseRepository.save(
            Exercise(
                exerciseId = ExerciseId(exerciseId, version),
                title = ed.title,
                prompt = ed.prompt,
                exerciseType = ed.exerciseType,
                lessonId = lessonId,
                isDeleted = false
            )
        )
        return exerciseId to version
    }

    private fun replaceOptions(exerciseId: UUID, version: Int, options: List<ExerciseOptionDiffRequest>) {
        exerciseOptionRepository.deleteByExerciseIdAndVersion(exerciseId, version)
        entityManager.flush()

        options.forEachIndexed { i, o ->
            val entity = ExerciseOption(
                id = null,                              // ← must be null for inserts
                content = o.content,
                answerOrder = o.answerOrder ?: i + 1,
                exerciseId = exerciseId,
                exerciseVersion = version
            )
            entityManager.persist(entity)              // insert
        }
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