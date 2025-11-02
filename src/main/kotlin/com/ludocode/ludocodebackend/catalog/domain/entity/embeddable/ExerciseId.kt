package com.ludocode.ludocodebackend.catalog.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class ExerciseId(
    @Column(name = "id")
    val id: UUID,

    @Column(name = "version")
    val version: Int
) : java.io.Serializable