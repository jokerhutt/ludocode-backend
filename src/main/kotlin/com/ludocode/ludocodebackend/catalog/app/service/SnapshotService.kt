package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ExerciseOptionDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.LessonDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.request.ModuleDiffRequest
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCLessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.admin.response.CCModuleResponse
import com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.admin.snapshot.OptionSnap
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
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.collections.forEach


@Service
class SnapshotService(
    private val lessonRepository: LessonRepository,
    private val moduleRepository: ModuleRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseOptionRepository: ExerciseOptionRepository,
    private val catalogService: CatalogService,
    @PersistenceContext private val entityManager: EntityManager
) {

    @Transactional
    fun applyModuleSnapshot(s: ModuleSnapshot): ModuleSnapshot {
        val module = moduleRepository.findByIdForUpdate(s.moduleId).orElseThrow()
        module.title = s.title
        moduleRepository.save(module)

        // delete lessons missing from snapshot
        val currentIds = lessonRepository.findActiveIdsByModule(module.id!!)
        val incomingIds = s.lessons.mapNotNull { it.id }.toSet()
        val toDelete = currentIds.filter { it !in incomingIds }
        if (toDelete.isNotEmpty()) lessonRepository.softDeleteIn(toDelete)

        // temp -> real
        val tempToReal = mutableMapOf<UUID, UUID>()
        s.lessons.forEach { ls -> tempToReal[ls.tempId] = ls.id ?: UUID.randomUUID() }

        // upserts
        s.lessons.forEach { ls -> upsertLesson(ls, module.id!!, tempToReal[ls.tempId]!!) }

        // ordering = snapshot order
        entityManager.flush(); entityManager.clear()
        lessonRepository.bumpAllInModule(module.id!!)
        s.lessons.forEachIndexed { idx, ls -> lessonRepository.setOrder(tempToReal[ls.tempId]!!, idx + 1) }
        entityManager.flush(); entityManager.clear()

        return buildModuleSnapshot(moduleId = module.id!!)

    }

    private fun upsertLesson(ls: LessonSnap, moduleId: UUID, realId: UUID) {
        val managed = lessonRepository.findById(realId).orElse(null)
        if (managed == null) {
            entityManager.persist(Lesson(id = realId, moduleId = moduleId, title = ls.title, isDeleted = false))
        } else {
            require(!managed.isDeleted) { "Lesson is deleted" }
            require(managed.moduleId == moduleId) { "Lesson not in module" }
            managed.title = ls.title
        }

        // delete exercises missing from snapshot
        val existingIds: Set<UUID> =
            exerciseRepository.findActiveExerciseIdsByLesson(realId).toSet()
        val keptIds: Set<UUID> = ls.exercises.mapNotNull { it.id }.toSet()
        val toDelete: List<UUID> = (existingIds - keptIds).toList()
        if (toDelete.isNotEmpty()) insertDeletedVersions(realId, toDelete)

        // upserts: new => v1, existing => bump
        ls.exercises.forEach { ex ->
            val exId = ex.id ?: UUID.randomUUID()
            val ver = if (ex.id == null) 1 else exerciseRepository.bumpVersion(exId)
            exerciseRepository.save(
                Exercise(
                    exerciseId = ExerciseId(exId, ver),
                    title = ex.title,
                    prompt = ex.prompt,
                    exerciseType = ex.exerciseType,
                    lessonId = realId,
                    isDeleted = false
                )
            )
            replaceOptions(exId, ver, ex.options)
        }
    }

    private fun replaceOptions(exerciseId: UUID, version: Int, options: List<OptionSnap>) {
        exerciseOptionRepository.deleteByExerciseIdAndVersion(exerciseId, version)
        entityManager.flush()
        options.forEachIndexed { i, o ->
            entityManager.persist(
                ExerciseOption(
                    id = null,
                    content = o.content,
                    answerOrder = o.answerOrder ?: i + 1,
                    exerciseId = exerciseId,
                    exerciseVersion = version
                )
            )
        }
    }

    private fun insertDeletedVersions(lessonId: UUID, exerciseIds: List<UUID>) {
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

    fun getSnapshotsByCourseId(courseId: UUID): List<ModuleSnapshot> {
        val moduleIds = moduleRepository.findModuleIdsByCourse(courseId)
        var moduleSnapshots = mutableListOf<ModuleSnapshot>()

        for (moduleId in moduleIds) {
            val snapshot = buildModuleSnapshot(moduleId)
            moduleSnapshots.add(snapshot)
        }

        return moduleSnapshots

    }

    fun buildModuleSnapshot(moduleId: UUID): ModuleSnapshot {
        val module = moduleRepository.findById(moduleId).orElseThrow()

        // assume lessons are returned ordered by order_index, id
        val lessons = lessonRepository.findAllByModuleId(moduleId)

        val lessonSnaps = lessons.map { l ->
            val exResponses = catalogService.getExercisesByLessonId(l.id!!)
            val exSnaps = exResponses.map { er ->
                ExerciseSnap(
                    id = er.id,
                    title = er.title,
                    prompt = er.prompt!!,
                    exerciseType = er.exerciseType,
                    options = er.exerciseOptions.map { opt ->
                        OptionSnap(
                            content = opt.content,
                            answerOrder = opt.answerOrder
                        )
                    }
                )
            }
            LessonSnap(
                id = l.id!!,
                tempId = l.id!!,
                title = l.title,
                orderIndex = l.orderIndex,
                exercises = exSnaps
            )
        }

        return ModuleSnapshot(
            moduleId = module.id!!,
            title = module.title!!,
            lessons = lessonSnaps
        )
    }

}