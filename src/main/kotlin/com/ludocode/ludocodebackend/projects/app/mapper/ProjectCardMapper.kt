package com.ludocode.ludocodebackend.projects.app.mapper
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardResponse
import com.ludocode.ludocodebackend.projects.infra.projection.ProjectCardProjection
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProjectCardMapper(private val basicMapper: BasicMapper) {

    fun toProjectCardResponseList(
        projectCardProjections: List<ProjectCardProjection>,
        technologiesByProjectId: Map<UUID, List<String>>,
        page: Int,
        totalPages: Int,
        hasNext: Boolean
    ): ProjectCardListResponse =
        ProjectCardListResponse(
            basicMapper.list(projectCardProjections) { project ->
                toProjectCardResponse(project, technologiesByProjectId[project.getProjectId()] ?: emptyList())
            },
            page,
            totalPages,
            hasNext
        )

    fun toProjectCardResponse(
        projectCardProjection: ProjectCardProjection,
        technologies: List<String>
    ): ProjectCardResponse {
        return ProjectCardResponse(
            projectId = projectCardProjection.getProjectId(),
            createdAt = projectCardProjection.getCreatedAt(),
            updatedAt = projectCardProjection.getUpdatedAt(),
            authorId = projectCardProjection.getAuthorId(),
            visibility = projectCardProjection.getVisibility(),
            projectType = projectCardProjection.getProjectType(),
            projectTitle = projectCardProjection.getProjectTitle(),
            technologies = technologies
                .distinctBy { it.lowercase() }
                .map { languageName -> languageName },
            deleteAt = projectCardProjection.getDeleteAt(),
        )
    }

}