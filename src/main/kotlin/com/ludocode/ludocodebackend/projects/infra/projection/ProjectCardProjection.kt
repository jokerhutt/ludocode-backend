package com.ludocode.ludocodebackend.projects.infra.projection

import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import java.time.OffsetDateTime
import java.util.UUID

interface ProjectCardProjection {
    fun getProjectId(): UUID
    fun getAuthorId(): UUID
    fun getProjectTitle(): String
    fun getCreatedAt(): OffsetDateTime
    fun getUpdatedAt(): OffsetDateTime
    fun getVisibility(): Visibility
    fun getProjectType(): ProjectType
    fun getDeleteAt(): OffsetDateTime?
}