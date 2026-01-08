package com.ludocode.ludocodebackend.playground.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.playground.app.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.playground.app.dto.request.RenameRequest
import com.ludocode.ludocodebackend.playground.app.service.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.PROJECTS.BASE)
class ProjectController(private val projectService: ProjectService) {

    @PutMapping(ApiPaths.PROJECTS.BY_ID)
    fun saveProject (@RequestBody projectSnapshot: ProjectSnapshot, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectSnapshot> {
         return ResponseEntity.ok(projectService.saveProjectSnapshot(projectSnapshot))
    }

    @DeleteMapping(ApiPaths.PROJECTS.BY_ID)
    fun deleteProject (@PathVariable projectId : UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.deleteProjectForUser(projectId, userId))
    }

    @PatchMapping(ApiPaths.PROJECTS.NAME)
    fun renameProject (@PathVariable projectId: UUID, @RequestBody request: RenameRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.renameProject(projectId, request, userId))
    }

    @PostMapping
    fun createProject (@RequestBody request: CreateProjectRequest, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.createProject(request, userId))
    }

    @GetMapping
    fun getUserProjects (@AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.getUserProjects(userId))
    }

    @GetMapping(ApiPaths.PROJECTS.BY_ID)
    fun getProject (@PathVariable projectId: UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectSnapshot> {
        return ResponseEntity.ok(projectService.getProjectSnapshotForUserByProjectId(projectId, userId))
    }


}