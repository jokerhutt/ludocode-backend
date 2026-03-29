package com.ludocode.ludocodebackend.projects.app.mapper
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

@Component
class ProjectMapper(private val basicMapper: BasicMapper) {

    fun toProjectFileSnapshot(projectFile: ProjectFile, fileContent: String?): ProjectFileSnapshot {
        return basicMapper.one(projectFile) {
            ProjectFileSnapshot(
                id = it.id,
                path = it.filePath,
                language = it.codeLanguage,
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
        projectType: ProjectType,
        updatedAt: OffsetDateTime?,
        deleteAt: OffsetDateTime?,
        projectFiles: List<ProjectFile>,
        fileContentMap: Map<String, String>,
        entryFilePath: String,
    ): ProjectSnapshot {
        return ProjectSnapshot(
            projectId,
            projectName,
            projectType,
            updatedAt,
            deleteAt = deleteAt,
            toProjectFileSnapshotList(projectFiles, fileContentMap),
            entryFilePath = entryFilePath,
        )
    }


}