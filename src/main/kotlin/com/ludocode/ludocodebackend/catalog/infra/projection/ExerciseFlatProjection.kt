package com.ludocode.ludocodebackend.catalog.infra.projection

import java.util.UUID

interface ExerciseFlatProjection {


    fun getExerciseId(): UUID
    fun getVersion(): Int
    fun getTitle(): String
    fun getPrompt(): String?
    fun getExerciseType(): String
    fun getLessonId(): UUID

    fun getOptionId(): UUID?
    fun getContent(): String?
    fun getAnswerOrder(): Int?

}