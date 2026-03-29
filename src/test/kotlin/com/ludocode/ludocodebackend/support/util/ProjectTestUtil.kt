package com.ludocode.ludocodebackend.support.util

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

object ProjectTestUtil {

    fun spawnProjects(
        amount: Int,
        userId: UUID,
        language: String,
        extension: String,
        starterContent: String,
        clock: Clock,
        storage: Storage,
        bucketName: String,
        startDaysAgo: Long = 1
    ): Pair<List<UserProject>, List<ProjectFile>> {

        val now = OffsetDateTime.now(clock)

        val projects = (0 until amount).map { index ->
            UserProject(
                id = UUID.randomUUID(),
                name = "P${index + 1}",
                userId = userId,
                projectType = ProjectType.CODE,
                createdAt = now.minusDays((10 + index).toLong()),
                updatedAt = now.minusDays(startDaysAgo + index),
                requestHash = UUID.randomUUID()
            )
        }

        val files = projects.map { project ->
            val fileId = UUID.randomUUID()
            val filePath = "main$extension"
            val contentUrl = "${project.id}/$filePath"

            storage.create(
                BlobInfo.newBuilder(bucketName, contentUrl).build(),
                starterContent.toByteArray(Charsets.UTF_8)
            )

            ProjectFile(
                id = fileId,
                projectId = project.id,
                contentUrl = contentUrl,
                filePath = filePath,
                codeLanguage = language
            )
        }

        projects.zip(files).forEach { (project, file) ->
            project.entryFilePath = file.filePath
        }



        return Pair(projects, files)
    }

}