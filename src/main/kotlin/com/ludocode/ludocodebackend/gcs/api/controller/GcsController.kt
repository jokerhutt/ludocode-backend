package com.ludocode.ludocodebackend.gcs.api.controller

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.gcs.app.port.`in`.GcsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(InternalPathConstants.IGCS)
class GcsController(
                    private val gcsUseCase: GcsUseCase
) {

    @GetMapping(InternalPathConstants.IGCS_GET_CONTENT_FROM_PATH)
    fun getContentFromPath(@RequestParam path: String): ResponseEntity<String> {
        println("GOT CALLED WITH: $path")
        return ResponseEntity.ok(gcsUseCase.getFileContentFromPath(path))
    }

    @PostMapping(InternalPathConstants.IGCS_UPLOAD_FILES)
    fun uploadDataList (@RequestBody request: GcsPutRequestList) : ResponseEntity<UploadedPaths> {
        return ResponseEntity.ok(gcsUseCase.uploadDataList(request))
    }

    @PostMapping(InternalPathConstants.IGCS_DELETE_FILES)
    fun deleteDataList (@RequestBody request: GcsDeleteRequestList) : ResponseEntity<UploadedPaths> {
        return ResponseEntity.ok(gcsUseCase.deleteDataList(request))
    }

    @GetMapping(InternalPathConstants.IGCS_GET_CONTENT_FROM_PATH_LIST)
    fun getContentFromUrls (@RequestParam paths: List<String>) : ResponseEntity<Map<String, String>> {
        println("Reached GCS Controller")
        return ResponseEntity.ok(gcsUseCase.getContentFromUrls(paths))
    }




}