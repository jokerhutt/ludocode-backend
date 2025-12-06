package com.ludocode.ludocodebackend.storage.app.port.`in`

import com.ludocode.ludocodebackend.storage.app.dto.request.MediaPutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths

interface StoragePortForServices {

    fun getContentFromUrls (paths: List<String>) : Map<String, String>
    fun uploadDataList (reqs: StoragePutRequestList): UploadedPaths
    fun getContentFromPath(path: String): String
    fun deleteDataList (req: StorageDeleteRequest): UploadedPaths
    fun uploadMedia (req: MediaPutRequest) : String
    fun getMedia(path: String): ByteArray

}