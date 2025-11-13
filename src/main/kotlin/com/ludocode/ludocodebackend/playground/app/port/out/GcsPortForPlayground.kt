package com.ludocode.ludocodebackend.playground.app.port.out

import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.UploadedPaths

interface GcsPortForPlayground {

    fun getContentFromUrls (bucket: String, paths: List<String>) : Map<String, String>
    fun uploadDataList (reqs: GcsPutRequestList): UploadedPaths
    fun deleteDataList (req: GcsDeleteRequestList): UploadedPaths

}