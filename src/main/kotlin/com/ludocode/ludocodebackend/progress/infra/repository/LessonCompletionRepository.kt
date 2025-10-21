package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LessonCompletionRepository : JpaRepository<LessonCompletion, UUID> {
}