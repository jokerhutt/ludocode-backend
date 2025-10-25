package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "exercise_attempt")
class ExerciseAttempt (

    @Id
    val id: UUID,

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "exercise_id")
    var exerciseId: UUID? = null,

    @Column(name = "score")
    val score: Int

)