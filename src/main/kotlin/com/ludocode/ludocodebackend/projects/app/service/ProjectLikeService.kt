package com.ludocode.ludocodebackend.projects.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectLikeCountResponse
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectLike
import com.ludocode.ludocodebackend.projects.domain.entity.embeddable.ProjectLikeId
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.projects.infra.repository.ProjectLikeRepository
import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectLikeService(
    private val userProjectRepository: UserProjectRepository,
    private val projectLikeRepository: ProjectLikeRepository
) {

    @Transactional
    fun likeProject(likerId: UUID, projectId: UUID) {

        val existingProject = userProjectRepository.findById(projectId)
            .orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

        if (existingProject.projectVisibility == Visibility.PRIVATE && existingProject.userId != likerId) {
            throw ApiException(ErrorCode.NOT_ALLOWED)
        }

        val likeId = ProjectLikeId(likerId, projectId)
        if (projectLikeRepository.existsById(likeId)) {
            return
        }

        projectLikeRepository.save(ProjectLike(projectLikeId = likeId))
    }

    @Transactional
    fun unlikeProject(likerId: UUID, projectId: UUID) {

        val existingProject = userProjectRepository.findById(projectId)
            .orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

        if (existingProject.projectVisibility == Visibility.PRIVATE && existingProject.userId != likerId) {
            throw ApiException(ErrorCode.NOT_ALLOWED)
        }

        projectLikeRepository.deleteById(ProjectLikeId(likerId, projectId))
    }

    fun getLikeCountsByProjectIds(projectIds: List<UUID>): List<ProjectLikeCountResponse> {
        if (projectIds.isEmpty()) return emptyList()

        val countsByProjectId = projectLikeRepository
            .countByProjectIds(projectIds)
            .associate { it.getProjectId() to it.getLikeCount().toInt() }

        return projectIds.map { projectId ->
            ProjectLikeCountResponse(
                id = projectId,
                count = countsByProjectId[projectId] ?: 0,
            )
        }
    }

}