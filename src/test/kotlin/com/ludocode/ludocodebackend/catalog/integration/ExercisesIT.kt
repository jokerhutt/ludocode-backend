package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.exercise.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.Test

class ExercisesIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun getExercises_returnsExercises() {

        val lesson1Id = py1L1
        val lesson2Id = py1L2

        val response = submitGetExercisesByLessonId(lesson1Id)
        val response2 = submitGetExercisesByLessonId(lesson2Id)

        assertThat(response).isNotEmpty()
        assertThat(response2).isNotEmpty()

        response.forEach { res ->
            assertThat(res.blocks).isNotEmpty()
            assertThat(res.orderIndex).isGreaterThan(0)
            assertThat(res.interaction).isInstanceOf(ClozeInteraction::class.java)
        }

        response2.forEach { res ->
            assertThat(res.blocks).isNotEmpty()
            assertThat(res.orderIndex).isGreaterThan(0)

            if (res.interaction != null) {
                assertThat(res.interaction).isNotInstanceOf(ClozeInteraction::class.java)
            }
        }
    }

    private fun submitGetExercisesByLessonId(
        lessonId: UUID
    ): List<ExerciseResponse> =
        TestRestClient
            .getOk(
                ApiPaths.LESSONS.byIdExercises(lessonId),
                user1.id!!,
                Array<ExerciseResponse>::class.java
            )
            .toList()
}