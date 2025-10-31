package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
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
    @PersistenceContext private val entityManager: EntityManager,
) {


    @Transactional
    fun applyCourseSnapshot(s: CourseSnap): CourseSnap {
        // 1) delete modules missing from snapshot
        val existing = moduleRepository.findActiveIdsByCourse(s.courseId).toSet()
        val incoming = s.modules.mapNotNull { it.moduleId }.toSet()
        val toDelete = existing - incoming
        if (toDelete.isNotEmpty()) moduleRepository.softDeleteIn(toDelete.toList())

        val tempToReal = mutableMapOf<UUID, UUID>()
        s.modules.forEach { ms ->
            val realId = ms.moduleId ?: UUID.randomUUID()
            tempToReal[ms.tempId] = realId
            upsertModule(ms, s.courseId, realId)
        }

        // 3) snapshot order → DB order
        entityManager.flush(); entityManager.clear()
        moduleRepository.bumpAllInCourse(s.courseId) // set order_index high to avoid unique conflicts
        s.modules.forEachIndexed { idx, ms ->
            moduleRepository.setOrder(tempToReal[ms.tempId]!!, idx + 1)
        }
        entityManager.flush(); entityManager.clear()

        // 4) apply lessons/exercises per module
        s.modules.forEach { ms ->
            applyModuleSnapshot(ms, tempToReal[ms.tempId]!!)
        }

        // 5) return a fresh rebuild
        return getSnapshotsByCourseId(s.courseId)
    }

    private fun upsertModule(ms: ModuleSnapshot, courseId: UUID, realId: UUID) {
        // If you're doing concurrent edits/order updates, prefer findByIdForUpdate(realId)
        val managed = moduleRepository.findById(realId).orElse(null)

        if (managed == null) {
            // brand-new module → create a NEW instance and persist
            entityManager.persist(
                Module(
                    id = realId,                 // you mapped temp → real UUID earlier
                    courseId = courseId,
                    title = ms.title,
                    orderIndex = 0,              // or ms.orderIndex ?: 0 if you carry it in the snapshot
                    isDeleted = false            // make sure Module has this column if you use soft-delete
                )
            )
        } else {
            // existing → mutate the MANAGED instance; DO NOT persist/merge here
            require(!managed.isDeleted) { "Module is deleted" }
            require(managed.courseId == courseId) { "Module not in course" }
            managed.title = ms.title
            // optionally keep/adjust orderIndex here; you already rewrite ordering afterward
        }
    }

    @Transactional
    fun applyModuleSnapshot(s: ModuleSnapshot, moduleId: UUID): ModuleSnapshot {
        // delete lessons missing from snapshot
        val currentIds = lessonRepository.findActiveIdsByModule(moduleId)
        val incomingIds = s.lessons.mapNotNull { it.id }.toSet()
        val toDelete = currentIds.filter { it !in incomingIds }
        if (toDelete.isNotEmpty()) lessonRepository.softDeleteIn(toDelete)

        // temp → real for lessons
        val tempToReal = mutableMapOf<UUID, UUID>()
        s.lessons.forEach { ls -> tempToReal[ls.tempId] = ls.id ?: UUID.randomUUID() }

        // upserts
        s.lessons.forEach { ls -> upsertLesson(ls, moduleId, tempToReal[ls.tempId]!!) }

        // ordering
        entityManager.flush(); entityManager.clear()
        lessonRepository.bumpAllInModule(moduleId)
        s.lessons.forEachIndexed { idx, ls ->
            lessonRepository.setOrder(tempToReal[ls.tempId]!!, idx + 1)
        }
        entityManager.flush(); entityManager.clear()

        return buildModuleSnapshot(moduleId)
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
        ls.exercises.forEachIndexed { idx, ex ->
            val exId = ex.id ?: UUID.randomUUID()
            val ver = if (ex.id == null) 1 else exerciseRepository.bumpVersion(exId)

            exerciseRepository.save(
                Exercise(
                    exerciseId = ExerciseId(exId, ver),
                    title = ex.title,
                    prompt = ex.prompt,
                    exerciseType = ex.exerciseType,
                    lessonId = realId,
                    isDeleted = false,
                    orderIndex = idx + 1
                )
            )

            val options = normalizeOptions(exId, ver, ex)
            replaceOptions(exId, ver, options)
        }

    }

    @Transactional
    fun replaceOptions(exerciseId: UUID, version: Int, options: List<ExerciseOption>) {
        exerciseOptionRepository.deleteByExerciseIdAndVersion(exerciseId, version)
        entityManager.flush()
        options.forEach { entityManager.persist(it) }
    }

    private fun normalizeOptions(
        exId: UUID,
        ver: Int,
        snap: ExerciseSnap
    ): List<ExerciseOption> {
        val correct = snap.correctOptions
            .filterNot { it.content.isBlank() }
            .mapIndexed { idx, o ->
                ExerciseOption(
                    id = null,
                    content = o.content.trim(),
                    answerOrder = idx + 1,           // 1..n
                    exerciseId = exId,
                    exerciseVersion = ver
                )
            }

        val distractors = snap.distractors
            .filterNot { it.content.isBlank() }
            .map { o ->
                ExerciseOption(
                    id = null,
                    content = o.content.trim(),
                    answerOrder = null,              // stays null
                    exerciseId = exId,
                    exerciseVersion = ver
                )
            }

        return correct + distractors
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
                    isDeleted = true,
                    orderIndex = 0
                )
            )
        }
    }

    fun getSnapshotsByCourseId(courseId: UUID): CourseSnap {
        val moduleIds = moduleRepository.findModuleIdsByCourse(courseId)
        var moduleSnapshots = mutableListOf<ModuleSnapshot>()

        for (moduleId in moduleIds) {
            val snapshot = buildModuleSnapshot(moduleId)
            moduleSnapshots.add(snapshot)
        }

        return CourseSnap(courseId = courseId, modules = moduleSnapshots)

    }

    fun buildModuleSnapshot(moduleId: UUID): ModuleSnapshot {
        val module = moduleRepository.findById(moduleId).orElseThrow()

        // assume lessons are returned ordered by order_index, id
        val lessons = lessonRepository.findAllByModuleId(moduleId)

        val lessonSnaps = lessons.map { l ->
            val exResponses = catalogService.getExercisesByLessonId(l.id!!)

            val exSnaps = exResponses.map { er ->

                val correctOptions = er.exerciseOptions
                    .filter { it.answerOrder != null }
                    .sortedBy { it.answerOrder }

                val distractors = er.exerciseOptions
                    .filter { it.answerOrder == null }

                ExerciseSnap(
                    id = er.id,
                    title = er.title,
                    prompt = er.prompt!!,
                    exerciseType = er.exerciseType,
                    correctOptions = correctOptions.map { opt ->
                        OptionSnap(
                            content = opt.content,
                            answerOrder = opt.answerOrder
                        )
                    },
                    distractors = distractors.map { opt ->
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
            tempId = module.id!!,
            title = module.title!!,
            lessons = lessonSnaps
        )
    }

}