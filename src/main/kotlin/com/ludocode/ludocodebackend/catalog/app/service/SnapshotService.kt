package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
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
    fun applyCourseSnapshot(s: CourseSnap): CourseSnap {
        val modules = s.modules
        var newModules = mutableListOf<ModuleSnapshot>()

        for (module: ModuleSnapshot in modules) {
            newModules.add(applyModuleSnapshot(module))
        }
        return CourseSnap(courseId = s.courseId, modules = newModules)
    }

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
                    isDeleted = true
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
            title = module.title!!,
            lessons = lessonSnaps
        )
    }

}