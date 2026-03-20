package com.ludocode.ludocodebackend.projects.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardResponse
import com.ludocode.ludocodebackend.projects.infra.projection.ProjectCardProjection
import org.springframework.stereotype.Component

@Component
class ProjectCardMapper (private val basicMapper: BasicMapper, private val projectMapper: ProjectMapper) {


    fun toProjectCardResponseList(
        projectCardProjections: List<ProjectCardProjection>,
        page: Int,
        totalPages: Int,
        hasNext: Boolean
    ): ProjectCardListResponse =
        ProjectCardListResponse(
            basicMapper.list(projectCardProjections) { project ->
                toProjectCardResponse(project)
            },
            page,
            totalPages,
            hasNext
        )


    fun toProjectCardResponse(
        projectCardProjection: ProjectCardProjection
    ): ProjectCardResponse {
        return ProjectCardResponse(
            projectId = projectCardProjection.getProjectId(),
            createdAt = projectCardProjection.getCreatedAt(),
            updatedAt = projectCardProjection.getUpdatedAt(),
            authorId = projectCardProjection.getAuthorId(),
            languageName = projectCardProjection.getLanguageName(),
            languageIconName = projectCardProjection.getLanguageIconName(),
            visibility = projectCardProjection.getVisibility(),
            projectTitle = projectCardProjection.getProjectTitle(),
            deleteAt = projectCardProjection.getDeleteAt()
        )
    }

}