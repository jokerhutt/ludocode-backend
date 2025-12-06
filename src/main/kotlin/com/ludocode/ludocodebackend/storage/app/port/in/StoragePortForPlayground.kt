package com.ludocode.ludocodebackend.storage.app.port.`in`

import com.ludocode.ludocodebackend.storage.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.storage.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths

interface StoragePortForPlayground {

    fun getContentFromUrls (paths: List<String>) : Map<String, String>
    fun uploadDataList (reqs: GcsPutRequestList): UploadedPaths
    fun getContentFromPath(path: String): String
    fun deleteDataList (req: GcsDeleteRequestList): UploadedPaths

}