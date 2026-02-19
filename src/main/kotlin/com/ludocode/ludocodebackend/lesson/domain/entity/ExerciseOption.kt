package com.ludocode.ludocodebackend.lesson.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "exercise_option")
class ExerciseOption(

    @Id
    var id: UUID,

    @Column(name = "exercise_id")
    var exerciseId: UUID,

    @Column(name = "exercise_version")
    var exerciseVersion: Int,

    @Column(name = "option_id", nullable = false)
    var optionId: UUID,

    @Column(name = "answer_order")
    val answerOrder: Int? = null

)