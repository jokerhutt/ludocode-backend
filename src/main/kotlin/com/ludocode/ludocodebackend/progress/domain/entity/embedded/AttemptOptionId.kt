package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import jakarta.persistence.Column
import java.util.UUID

data class AttemptOptionId(

    @Column(name = "attempt_id", nullable = false)
    val attemptId: UUID,

    @Column(name = "exercise_option_id", nullable = false)
    var exerciseOptionId: UUID

) : java.io.Serializable
