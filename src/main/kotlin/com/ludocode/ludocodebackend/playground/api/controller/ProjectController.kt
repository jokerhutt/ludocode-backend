package com.ludocode.ludocodebackend.playground.api.controller
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.service.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(PathConstants.PROJECT)
class ProjectController(private val projectService: ProjectService) {

    @PostMapping(PathConstants.SAVE_PROJECT)
    fun saveProject (@PathVariable pid : UUID, @RequestBody projectSnapshot: ProjectSnapshot, @AuthenticationPrincipal(expression = "userId") userId: UUID) : ResponseEntity<ProjectSnapshot> {
         println("Reached save project controller")
         return ResponseEntity.ok(projectService.saveProjectSnapshot(projectSnapshot))
    }

    @GetMapping(PathConstants.GET_PROJECT)
    fun getProject (@PathVariable pid: UUID) : ResponseEntity<ProjectSnapshot> {
        return ResponseEntity.ok(projectService.getProjectSnapshotByProjectId(pid))
    }


}