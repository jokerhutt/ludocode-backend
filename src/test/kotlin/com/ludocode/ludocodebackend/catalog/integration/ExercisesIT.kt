package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.Test

class ExercisesIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getExercises_returnsExercises() {

        val lesson1Id = py1L1
        val lesson2Id = py1L2

        val response: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson1Id)
        val response2: List<ExerciseResponse> = submitGetExercisesByLessonId(lesson2Id)

        assertThat(response).isNotEmpty()
        assertThat(response2).isNotEmpty()

        for (res: ExerciseResponse in response) {
            assertThat(res.correctOptions.size).isGreaterThanOrEqualTo(1)
            assertThat(res.lessonId).isEqualTo(lesson1Id)
            assertThat(res.title).isNotNull()
            assertThat(res.prompt).isNotNull()
            assertThat(res.exerciseType).isEqualTo(ExerciseType.CLOZE)
        }

        for (res: ExerciseResponse in response2) {
            assertThat(res.correctOptions.size).isGreaterThanOrEqualTo(1)
            assertThat(res.exerciseType).isNotEqualTo(ExerciseType.CLOZE)
            assertThat(res.lessonId).isEqualTo(lesson2Id)
        }

    }

    private fun submitGetExercisesByLessonId(
        lessonId: UUID
    ): List<ExerciseResponse> =
        TestRestClient
            .getOk(ApiPaths.CATALOG.lessonExercises(lessonId),
                user1.id!!,
                Array<ExerciseResponse>::class.java)
            .toList()}