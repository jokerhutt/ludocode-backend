package com.ludocode.ludocodebackend.progress.infra

import com.ludocode.ludocodebackend.progress.domain.LessonCompletion
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LessonCompletionRepository : JpaRepository<LessonCompletion, UUID> {
}