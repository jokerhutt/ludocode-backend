package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.progress.domain.entity.embedded.AttemptOptionId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "attempt_option")
class AttemptOption(
    @EmbeddedId
    val attemptOptionId: AttemptOptionId
)