package com.ludocode.ludocodebackend.projects.infra.projection

import java.util.UUID

interface ProjectLikeCountProjection {
    fun getProjectId(): UUID
    fun getLikeCount(): Long
}

