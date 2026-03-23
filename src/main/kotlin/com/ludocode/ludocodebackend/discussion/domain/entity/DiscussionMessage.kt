package com.ludocode.ludocodebackend.discussion.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "discussion_message")
class DiscussionMessage (

    @Id
    var id: UUID,

    @Column(name = "discussion_id")
    val discussionId: UUID,

    @Column(name = "author_id")
    val authorId: UUID,

    @Column(name = "parent_id")
    val parentId: UUID?,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime

    )