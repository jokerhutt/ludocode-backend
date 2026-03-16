package com.ludocode.ludocodebackend.projects.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ProjectPlanEnforcer(private val userProjectRepository: UserProjectRepository, private val clock: Clock) {

    private val logger = LoggerFactory.getLogger(ProjectPlanEnforcer::class.java)

    @Transactional
    fun enforcePlanLimit(userId: UUID, maxProjects: Int) {
        withMdc(LogFields.USER_ID to userId.toString()) {
            val projects = userProjectRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)

            logger.info(
                LogEvents.PROJECT_PLAN_ENFORCEMENT_STARTED + " {} {}",
                kv(LogFields.PROJECT_COUNT, projects.size),
                kv(LogFields.MAX_PROJECTS, maxProjects)
            )

            if (projects.size <= maxProjects) {
                projects.forEach {
                    if (it.deleteAt != null) {
                        it.deleteAt = null
                    }
                }
                logger.info(
                    LogEvents.PROJECT_PLAN_ENFORCEMENT_COMPLETED + " {}",
                    kv(LogFields.MARKED_FOR_DELETION_COUNT, 0)
                )
                return
            }

            val deleteAt = OffsetDateTime.now(clock).plusWeeks(2)
            var markedForDeletionCount = 0

            projects.forEachIndexed { index, project ->
                if (index < maxProjects) {
                    if (project.deleteAt != null) {
                        project.deleteAt = null
                    }
                } else {
                    if (project.deleteAt == null) {
                        project.deleteAt = deleteAt
                        markedForDeletionCount++
                    }
                }
            }

            userProjectRepository.saveAll(projects)

            logger.info(
                LogEvents.PROJECT_PLAN_ENFORCEMENT_COMPLETED + " {}",
                kv(LogFields.MARKED_FOR_DELETION_COUNT, markedForDeletionCount)
            )
        }
    }

}