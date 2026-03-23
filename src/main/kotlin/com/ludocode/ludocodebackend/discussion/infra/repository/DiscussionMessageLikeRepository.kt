package com.ludocode.ludocodebackend.discussion.infra.repository

import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessageLike
import com.ludocode.ludocodebackend.discussion.domain.entity.embeddable.DiscussionMessageLikeId
import com.ludocode.ludocodebackend.discussion.infra.projection.DiscussionMessageLikeCountProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DiscussionMessageLikeRepository : JpaRepository<DiscussionMessageLike, DiscussionMessageLikeId> {

	@Query(
		"""
		SELECT dml.discussionMessageLikeId.messageId AS messageId, COUNT(dml) AS likeCount
		FROM DiscussionMessageLike dml
		WHERE dml.discussionMessageLikeId.messageId IN :messageIds
		GROUP BY dml.discussionMessageLikeId.messageId
		"""
	)
	fun countByMessageIds(@Param("messageIds") messageIds: List<UUID>): List<DiscussionMessageLikeCountProjection>

	@Query(
		"""
		SELECT COUNT(dml)
		FROM DiscussionMessageLike dml
		WHERE dml.discussionMessageLikeId.messageId = :messageId
		"""
	)
	fun countByMessageId(@Param("messageId") messageId: UUID): Long

	@Query(
		"""
		SELECT dml.discussionMessageLikeId.messageId
		FROM DiscussionMessageLike dml
		WHERE dml.discussionMessageLikeId.userId = :userId
		AND dml.discussionMessageLikeId.messageId IN :messageIds
		"""
	)
	fun findMessageIdsLikedByUser(
		@Param("userId") userId: UUID,
		@Param("messageIds") messageIds: List<UUID>
	): List<UUID>


}