package com.ludocode.ludocodebackend.gcs.app.port.`in`

import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.response.UploadedPaths

interface GcsPortForPlayground {

    fun getContentFromUrls (paths: List<String>) : Map<String, String>
    fun uploadDataList (reqs: GcsPutRequestList): UploadedPaths
    fun getContentFromPath(path: String): String
    fun deleteDataList (req: GcsDeleteRequestList): UploadedPaths

}