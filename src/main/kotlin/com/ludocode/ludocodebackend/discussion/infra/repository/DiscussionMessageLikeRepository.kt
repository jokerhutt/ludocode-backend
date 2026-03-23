package com.ludocode.ludocodebackend.discussion.infra.repository

import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessageLike
import com.ludocode.ludocodebackend.discussion.domain.entity.embeddable.DiscussionMessageLikeId
import org.springframework.data.jpa.repository.JpaRepository

interface DiscussionMessageLikeRepository : JpaRepository<DiscussionMessageLike, DiscussionMessageLikeId> {



}