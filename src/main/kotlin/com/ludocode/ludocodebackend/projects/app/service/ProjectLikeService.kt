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

    fun getLikeCountByProjectId(
        userId: UUID,
        projectId: UUID
    ): ProjectLikeCountResponse {

        val count = projectLikeRepository
            .countByProjectId(projectId)
            .toInt()

        val likedByMe = projectLikeRepository
            .existsById(ProjectLikeId(userId, projectId))

        return ProjectLikeCountResponse(
            id = projectId,
            count = count,
            likedByMe = likedByMe
        )
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

    fun getLikeCountsByProjectIds(
        userId: UUID,
        projectIds: List<UUID>
    ): List<ProjectLikeCountResponse> {

        val counts = projectLikeRepository.countByProjectIds(projectIds)
            .associate { it.getProjectId() to it.getLikeCount().toInt() }

        val likedSet = projectLikeRepository
            .findProjectIdsLikedByUser(userId, projectIds)
            .toSet()

        return projectIds.map { id ->
            ProjectLikeCountResponse(
                id = id,
                count = counts[id] ?: 0,
                likedByMe = id in likedSet
            )
        }
    }

}