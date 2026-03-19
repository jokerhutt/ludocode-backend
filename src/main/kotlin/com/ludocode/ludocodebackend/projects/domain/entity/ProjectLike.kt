package com.ludocode.ludocodebackend.projects.domain.entity

import com.ludocode.ludocodebackend.projects.domain.entity.embeddable.ProjectLikeId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "project_like")
class ProjectLike (

    @EmbeddedId
    val projectLikeId: ProjectLikeId

)