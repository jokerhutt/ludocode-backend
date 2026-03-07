package com.ludocode.ludocodebackend.tag.api.controller.admin

import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import com.ludocode.ludocodebackend.tag.app.service.TagService
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Admin Tags",
    description = "Admin operations related to creating and modifying course tags"
)
@Profile("admin", "dev", "test", "devtestadmin")
@RestController
@RequestMapping(ApiPaths.TAGS.ADMIN_BASE)
class TagAdminController(
    private val tagService: TagService
) {

    @Operation(
        summary = "Create tag",
        description = """
        Creates a new Tag.
        The tag can later be assigned to courses and used to organize the catalog.
        Intended for catalog categorisation & admin use.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    fun createTag(
        @RequestBody req: TagMetadata
    ): ResponseEntity<List<TagMetadata>> {
        return ResponseEntity.ok(tagService.createTag(req))
    }

    @Operation(
        summary = "Update subject",
        description = """
        Updates the metadata of an existing course subject.
        Intended for catalog categorisation & admin use.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping(ApiPaths.TAGS.BY_TAG)
    fun updateTag(
        @PathVariable tagId: Long,
        @RequestBody req: TagMetadata
    ): ResponseEntity<List<TagMetadata>> {
        return ResponseEntity.ok(tagService.updateTag(tagId, req))
    }

    @Operation(
        summary = "Delete tag",
        description = """
        Deletes the specified tag.
        Intended for catalog categorisation & admin use.
        """
    )
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping(ApiPaths.TAGS.BY_TAG)
    fun deleteTag(
        @PathVariable subjectId: Long
    ): ResponseEntity<List<TagMetadata>> {
        return ResponseEntity.ok(tagService.deleteTag(subjectId))
    }

}