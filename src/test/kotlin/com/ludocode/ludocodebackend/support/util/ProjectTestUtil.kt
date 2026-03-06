package com.ludocode.ludocodebackend.support.util

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

object ProjectTestUtil {

    fun spawnProjects(
        amount: Int,
        userId: UUID,
        language: CodeLanguages,
        clock: Clock,
        storage: Storage,
        bucketName: String,
        startDaysAgo: Long = 1
    ): Pair<List<UserProject>, List<ProjectFile>> {

        val now = OffsetDateTime.now(clock)
        val starterContent = language.initialScript ?: ""

        val projects = (0 until amount).map { index ->
            UserProject(
                id = UUID.randomUUID(),
                name = "P${index + 1}",
                userId = userId,
                codeLanguage = language,
                createdAt = now.minusDays((10 + index).toLong()),
                updatedAt = now.minusDays(startDaysAgo + index),
                requestHash = UUID.randomUUID()
            )
        }

        val files = projects.map { project ->
            val fileId = UUID.randomUUID()
            val contentUrl = "${project.id}/$fileId"

            storage.create(
                BlobInfo.newBuilder(bucketName, contentUrl).build(),
                starterContent.toByteArray(Charsets.UTF_8)
            )

            ProjectFile(
                id = fileId,
                projectId = project.id,
                contentUrl = contentUrl,
                contentHash = "testhash-${project.id}",
                filePath = "main${language.extension}",
                codeLanguage = language
            )
        }

        projects.zip(files).forEach { (project, file) ->
            project.entryFileId = file.id
        }



        return Pair(projects, files)
    }

}