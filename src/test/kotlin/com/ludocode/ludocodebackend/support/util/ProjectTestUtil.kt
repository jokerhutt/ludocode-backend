package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

object ProjectTestUtil {

    fun spawnProjects(
        amount: Int,
        userId: UUID,
        language: CodeLanguages,
        clock: Clock,
        startDaysAgo: Long = 1
    ): List<UserProject> {

        val now = OffsetDateTime.now(clock)

        return (0 until amount).map { index ->
            UserProject(
                id = UUID.randomUUID(),
                name = "P${index + 1}",
                userId = userId,
                codeLanguage = language,
                createdAt = now.minusDays(10),
                updatedAt = now.minusDays(startDaysAgo + index),
                requestHash = UUID.randomUUID()
            )
        }
    }

}