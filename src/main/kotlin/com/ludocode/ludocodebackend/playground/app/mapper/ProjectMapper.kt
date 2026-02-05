package com.ludocode.ludocodebackend.playground.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.playground.api.dto.request.LanguageMetadata
import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.CodeLanguages
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ProjectMapper (private val basicMapper: BasicMapper) {

    fun toProjectFileSnapshot(projectFile: ProjectFile, fileContent: String?): ProjectFileSnapshot  {
        return basicMapper.one(projectFile) {
            ProjectFileSnapshot(
                id = it.id,
                path = it.filePath,
                language = toLanguageMetadata(it.codeLanguage),
                content = fileContent ?: ""
            )
        }}

    fun toProjectFileSnapshotList(projectFiles: List<ProjectFile>, fileContentMap: Map<String, String>): List<ProjectFileSnapshot> =
        basicMapper.list(projectFiles) { file ->
            toProjectFileSnapshot(file, fileContentMap[file.contentUrl])
        }

    fun toProjectSnapshot(projectId: UUID, projectName: String, projectLanguage: CodeLanguages, updatedAt: OffsetDateTime?, projectFiles: List<ProjectFile>, fileContentMap: Map<String, String>) : ProjectSnapshot {
        return ProjectSnapshot(projectId, projectName, toLanguageMetadata(language = projectLanguage), updatedAt, toProjectFileSnapshotList(projectFiles, fileContentMap))
    }

    fun toLanguageMetadata(language: CodeLanguages) : LanguageMetadata {
        return LanguageMetadata(
            name = language.name,
            languageId = language.id,
            editorId = language.editorId,
            initialScript = language.initialScript ?: "",
            slug = language.slug,
            pistonId = language.pistonId,
            extension = language.extension,
            base = language.base,
            iconName = language.iconName
        )
    }

    fun toLanguageMetadataList(languages: List<CodeLanguages>) : List<LanguageMetadata> {
        return basicMapper.list(languages) {language -> toLanguageMetadata(language)}
    }


}