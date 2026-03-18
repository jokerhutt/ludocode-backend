package com.ludocode.ludocodebackend.projects.api.controller

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.projects.api.dto.request.ChangeVisibilityRequest
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.request.RenameProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.projects.app.service.ProjectService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(
    name = "Projects",
    description = "Operations related to code projects & their content"
)
@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping(ApiPaths.PROJECTS.BASE)
class ProjectController(private val projectService: ProjectService) {

    @Operation(
        summary = "Save project for the selected project id",
        description = """
        Applies a project snapshot to the specified project.
        This operation replaces the existing files & contents of the project with the provided snapshot.
        Returns the persisted project snapshot after the update.
        Requires an authenticated user session and ownership of the project. 
        """
    )
    @PutMapping(ApiPaths.PROJECTS.BY_ID)
    fun saveProject(
        @RequestBody projectSnapshot: ProjectSnapshot,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<ProjectSnapshot> {
        return ResponseEntity.ok(projectService.saveProjectSnapshot(projectSnapshot))
    }

    @PutMapping("${ApiPaths.PROJECTS.BY_ID}${ApiPaths.PROJECTS.VISIBILITY}")
    fun updateVisibility(@PathVariable projectId : UUID, @AuthenticationPrincipal(expression = "userId") userId: UUID, @RequestBody req: ChangeVisibilityRequest) : ResponseEntity<ProjectListResponse> {
        projectService.changeProjectVisibility(projectId, userId, req.value)
        return ResponseEntity.ok(projectService.getUserProjects(userId))
    }

    @Operation(
        summary = "Delete project for the selected project id",
        description = """
        Deletes metadata and associated content for the specified project.
        Returns a list of the user's remaining projects after the delete operation.
        Requires an authenticated user session and ownership of the project
        """
    )
    @DeleteMapping(ApiPaths.PROJECTS.BY_ID)
    fun deleteProject(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<ProjectListResponse> {
        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            ResponseEntity.ok(projectService.deleteProjectForUser(projectId, userId))
        }
    }

    @Operation(
        summary = "Rename project",
        description = """
        Updates the name of an existing project owned by the currently authenticated user.
        Only the project name is modified; all other project data remains unchanged.
        Returns the updated list of the user's projects.
        Requires a valid session cookie to be present.
        """
    )
    @PatchMapping(ApiPaths.PROJECTS.NAME)
    fun renameProject(
        @PathVariable projectId: UUID,
        @RequestBody request: RenameProjectRequest,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<ProjectListResponse> {
        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            ResponseEntity.ok(projectService.renameProject(request, userId))
        }
    }

    @Operation(
        summary = "Create new project",
        description = """
        Creates a new project for the currently authenticated user.
        Returns the updated list of the user's projects including the newly created project.
        Requires a valid session cookie to be present. 
        """
    )
    @PostMapping
    fun createProject(
        @RequestBody request: CreateProjectRequest,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.createProject(request, userId))
    }

    @Operation(
        summary = "Get user's projects",
        description = """
        Returns all projects owned by the currently authenticated user.
        Requires a valid session cookie to be present. 
        """
    )
    @GetMapping
    fun getUserProjects(@AuthenticationPrincipal(expression = "userId") userId: UUID): ResponseEntity<ProjectListResponse> {
        return ResponseEntity.ok(projectService.getUserProjects(userId))
    }

    @Operation(
        summary = "Get project for the selected project id",
        description = """
        Returns the project associated with the specified project id.
        The project includes its metadata and all associated files.
        Requires an authenticated user session and ownership of the project. 
        """
    )
    @GetMapping(ApiPaths.PROJECTS.BY_ID)
    fun getProject(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal(expression = "userId") userId: UUID
    ): ResponseEntity<ProjectSnapshot> {
        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            ResponseEntity.ok(projectService.getProjectSnapshotForUserByProjectId(projectId, userId))
        }
    }


}