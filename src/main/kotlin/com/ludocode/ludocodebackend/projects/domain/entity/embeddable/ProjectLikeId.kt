package com.ludocode.ludocodebackend.projects.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class ProjectLikeId (

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "project_id")
    val projectId: UUID

)