package com.ludocode.ludocodebackend.discussion.infra.repository

import com.ludocode.ludocodebackend.discussion.domain.entity.Discussion
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DiscussionRepository : JpaRepository<Discussion, UUID> {
}