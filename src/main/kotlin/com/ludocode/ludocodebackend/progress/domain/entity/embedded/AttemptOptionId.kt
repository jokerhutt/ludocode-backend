package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class AttemptOptionId(

    @Column(name = "attempt_id")
    val attemptId: UUID,

    @Column(name = "exercise_option_id")
    var exerciseOptionId: UUID

)
