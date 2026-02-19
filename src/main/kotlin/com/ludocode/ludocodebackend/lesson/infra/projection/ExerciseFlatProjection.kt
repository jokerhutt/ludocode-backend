package com.ludocode.ludocodebackend.lesson.infra.projection

import java.util.*

interface ExerciseFlatProjection {


    fun getExerciseId(): UUID
    fun getVersion(): Int
    fun getTitle(): String
    fun getPrompt(): String?
    fun getExerciseType(): String
    fun getLessonId(): UUID
    fun getOrderIndex(): Int?
    fun getSubtitle(): String?
    fun getExerciseMedia(): String?
    fun getOptionId(): UUID?
    fun getContent(): String?
    fun getAnswerOrder(): Int?

}