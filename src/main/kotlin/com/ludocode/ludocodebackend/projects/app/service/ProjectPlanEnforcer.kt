package com.ludocode.ludocodebackend.projects.app.service

import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ProjectPlanEnforcer(private val userProjectRepository: UserProjectRepository, private val clock: Clock) {

    @Transactional
    fun enforcePlanLimit(userId: UUID, maxProjects: Int) {
        val projects = userProjectRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)

        if (projects.size <= maxProjects) {
            projects.forEach {
                if (it.deleteAt != null) {
                    it.deleteAt = null
                }
            }
            return
        }

        val deleteAt = OffsetDateTime.now(clock).plusWeeks(2)

        projects.forEachIndexed { index, project ->
            if (index < maxProjects) {
                if (project.deleteAt != null) {
                    project.deleteAt = null
                }
            } else {
                if (project.deleteAt == null) {
                    project.deleteAt = deleteAt
                }
            }
        }
        userProjectRepository.saveAll(projects)
    }

}