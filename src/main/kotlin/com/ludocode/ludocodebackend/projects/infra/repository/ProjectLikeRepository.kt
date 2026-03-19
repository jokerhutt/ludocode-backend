package com.ludocode.ludocodebackend.projects.infra.repository

import com.ludocode.ludocodebackend.projects.domain.entity.ProjectLike
import com.ludocode.ludocodebackend.projects.domain.entity.embeddable.ProjectLikeId
import com.ludocode.ludocodebackend.projects.infra.projection.ProjectLikeCountProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProjectLikeRepository : JpaRepository<ProjectLike, ProjectLikeId> {

	@Query(
		"""
		SELECT pl.projectLikeId.projectId AS projectId, COUNT(pl) AS likeCount
		FROM ProjectLike pl
		WHERE pl.projectLikeId.projectId IN :projectIds
		GROUP BY pl.projectLikeId.projectId
		"""
	)
	fun countByProjectIds(@Param("projectIds") projectIds: List<UUID>): List<ProjectLikeCountProjection>

}