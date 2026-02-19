package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "exercise_attempt")
class ExerciseAttempt(

    @Id
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "exercise_id", nullable = false)
    var exerciseId: UUID,

    @Column(name = "exercise_version", nullable = false)
    val exerciseVersion: Int

)