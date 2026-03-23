package com.ludocode.ludocodebackend.discussion.infra.repository

import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DiscussionMessageRepository : JpaRepository<DiscussionMessage, UUID> {
    fun findByDiscussionId(discussionId: UUID): List<DiscussionMessage>
    fun findByDiscussionIdOrderByCreatedAtAsc(discussionId: UUID): List<DiscussionMessage>
}