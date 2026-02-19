package com.ludocode.ludocodebackend.lesson.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class ExerciseId(
    @Column(name = "id")
    val id: UUID,

    @Column(name = "version_number")
    val versionNumber: Int
)