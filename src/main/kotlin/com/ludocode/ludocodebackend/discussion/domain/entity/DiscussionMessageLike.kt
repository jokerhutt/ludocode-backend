package com.ludocode.ludocodebackend.discussion.domain.entity

import com.ludocode.ludocodebackend.discussion.domain.entity.embeddable.DiscussionMessageLikeId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "discussion_message_like")
class DiscussionMessageLike (

   @EmbeddedId
    val discussionMessageLikeId: DiscussionMessageLikeId
)