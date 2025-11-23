package com.ludocode.ludocodebackend.playground.api.controller.internal

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.playground.app.port.`in`.PlaygroundUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(InternalPathConstants.IPROJECTS)
class InternalProjectController(private val playgroundUseCase: PlaygroundUseCase) {

    @GetMapping(InternalPathConstants.IPROJECTS_FILE_CONTENT_BY_ID)
    fun getFileContentFromFileId (@PathVariable fileId: UUID) : ResponseEntity<String> {
        return ResponseEntity.ok(playgroundUseCase.getFileContentById(fileId))
    }

}