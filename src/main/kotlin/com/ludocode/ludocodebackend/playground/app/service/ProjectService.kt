package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.mapper.ProjectMapper
import com.ludocode.ludocodebackend.playground.app.port.out.GcsPortForPlayground
import com.ludocode.ludocodebackend.playground.infra.http.GcsClientForPlayground
import com.ludocode.ludocodebackend.playground.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.playground.infra.repository.UserProjectRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class ProjectService(
    private val userProjectRepository: UserProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val gcsClientForPlayground: GcsClientForPlayground,
    private val projectMapper: ProjectMapper,
) {


    fun getProjectSnapshotByProjectId (projectId: UUID) : ProjectSnapshot {
        val bucketName = "ludo-file-content"
        val projectName = userProjectRepository.getProjectNameById(projectId)
        val projectFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)
        val fileContentUrls = projectFiles.map { it -> it.contentUrl }
        val fileContentsMap = gcsClientForPlayground.getContentFromUrls(bucketName, fileContentUrls)
        return projectMapper.toProjectSnapshot(projectId, projectName, projectFiles, fileContentsMap)
    }









}