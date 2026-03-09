package com.ludocode.ludocodebackend.feedback.infra.repository

import com.ludocode.ludocodebackend.feedback.domain.entity.Feedback
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeedbackRepository : JpaRepository<Feedback, UUID> {



}