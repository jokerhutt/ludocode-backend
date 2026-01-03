package com.ludocode.ludocodebackend.playground.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.intellij.lang.annotations.Language
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ProjectMapper (private val basicMapper: BasicMapper) {

    fun toProjectFileSnapshot(projectFile: ProjectFile, fileContent: String?): ProjectFileSnapshot  {
        println("ID: " + projectFile.id)
        println(("Content: +" + fileContent) ?: "")
        return basicMapper.one(projectFile) {
            ProjectFileSnapshot(
                id = it.id,
                path = it.filePath,
                language = it.fileLanguage,
                content = fileContent ?: ""
            )
        }}

    fun toProjectFileSnapshotList(projectFiles: List<ProjectFile>, fileContentMap: Map<String, String>): List<ProjectFileSnapshot> =
        basicMapper.list(projectFiles) { file ->
            println("FileName: " + file.filePath + "FileUrl: " + file.contentUrl)
            toProjectFileSnapshot(file, fileContentMap[file.contentUrl])
        }

    fun toProjectSnapshot(projectId: UUID, projectName: String, projectLanguage: LanguageType, updatedAt: OffsetDateTime?, projectFiles: List<ProjectFile>, fileContentMap: Map<String, String>) : ProjectSnapshot {
        println("updatedAt in DTO = ${updatedAt}")
        return ProjectSnapshot(projectId, projectName, projectLanguage, updatedAt, toProjectFileSnapshotList(projectFiles, fileContentMap))
    }


}