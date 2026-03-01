package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.projects.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime

@Component
class ProjectCleanupJob(
    private val userProjectRepository: UserProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val storagePort: StoragePortForServices,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(ProjectCleanupJob::class.java)

    @Transactional
    fun execute() {

        val now = OffsetDateTime.now(clock)

        logger.info(
            LogEvents.PROJECT_CLEANUP_STARTED + " {}",
            kv(LogFields.SCHEDULED_AT, now.toString())
        )

        val projects = userProjectRepository.findAllReadyForDeletion(now)

        if (projects.isEmpty()) return

        var deletedCount = 0

        projects.forEach { project ->

            val files = projectFileRepository
                .findAllProjectFilesByProjectId(project.id)

            val paths = files.map { it.contentUrl }

            try {

                if (paths.isNotEmpty()) {
                    storagePort.deleteList(StorageDeleteRequest(paths))
                }

                projectFileRepository.deleteAll(files)
                userProjectRepository.delete(project)

                deletedCount++

            } catch (ex: Exception) {

                logger.error(
                    LogEvents.PROJECT_CLEANUP_FAILED + " {} {}",
                    kv(LogFields.PROJECT_ID, project.id.toString()),
                    kv(LogFields.DELETE_AT, project.deleteAt?.toString()),
                    ex
                )
            }
        }

        logger.info(
            LogEvents.PROJECT_CLEANUP_COMPLETED + " {}",
            kv(LogFields.DELETED_COUNT, deletedCount)
        )
    }
}