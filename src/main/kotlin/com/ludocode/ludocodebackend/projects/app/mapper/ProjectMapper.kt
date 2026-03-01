package com.ludocode.ludocodebackend.projects.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class ProjectMapper(private val basicMapper: BasicMapper, private val languagesMapper: LanguagesMapper) {

    fun toProjectFileSnapshot(projectFile: ProjectFile, fileContent: String?): ProjectFileSnapshot {
        return basicMapper.one(projectFile) {
            ProjectFileSnapshot(
                id = it.id,
                path = it.filePath,
                language = languagesMapper.toLanguageMetadata(it.codeLanguage),
                content = fileContent ?: ""
            )
        }
    }

    fun toProjectFileSnapshotList(
        projectFiles: List<ProjectFile>,
        fileContentMap: Map<String, String>
    ): List<ProjectFileSnapshot> =
        basicMapper.list(projectFiles) { file ->
            toProjectFileSnapshot(file, fileContentMap[file.contentUrl])
        }

    fun toProjectSnapshot(
        projectId: UUID,
        projectName: String,
        projectLanguage: CodeLanguages,
        updatedAt: OffsetDateTime?,
        deleteAt: OffsetDateTime?,
        projectFiles: List<ProjectFile>,
        fileContentMap: Map<String, String>
    ): ProjectSnapshot {
        return ProjectSnapshot(
            projectId,
            projectName,
            languagesMapper.toLanguageMetadata(language = projectLanguage),
            updatedAt,
            deleteAt = deleteAt,
            toProjectFileSnapshotList(projectFiles, fileContentMap)
        )
    }


}