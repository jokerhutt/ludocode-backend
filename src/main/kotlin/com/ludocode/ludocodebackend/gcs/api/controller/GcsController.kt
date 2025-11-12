package com.ludocode.ludocodebackend.gcs.api.controller

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.port.GcsUseCase
import com.ludocode.ludocodebackend.gcs.app.service.GcsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(InternalPathConstants.IGCS)
class GcsController(private val storage: Storage, private val gcsService: GcsService,
                    private val gcsUseCase: GcsUseCase
) {

    @PostMapping("/send-data")
    fun sendData (@RequestBody request: GcsPutRequest) : String {
        return gcsService.uploadData(request)
    }

    @GetMapping(InternalPathConstants.IGCS_GET_CONTENT_FROM_PATHS)
    fun getContentFromUrls (@RequestParam bucketName : String, @RequestParam paths: List<String>) : ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(gcsUseCase.getContentFromUrls(bucketName, paths))
    }


}