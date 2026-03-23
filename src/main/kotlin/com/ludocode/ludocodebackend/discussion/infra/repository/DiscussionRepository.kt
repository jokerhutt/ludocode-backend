package com.ludocode.ludocodebackend.discussion.infra.repository

import com.ludocode.ludocodebackend.discussion.domain.entity.Discussion
import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DiscussionRepository : JpaRepository<Discussion, UUID> {

    fun findByEntityIdAndDiscussionTopic(entityId: UUID, discussionTopic: DiscussionTopic): Discussion?

}