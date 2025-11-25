package com.ludocode.ludocodebackend.gcs.app.port.`in`

import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.response.UploadedPaths

interface GcsUseCase {

    fun getFileContentFromPath(path: String): String
    fun getContentFromUrls (paths: List<String>): Map<String, String>
    fun uploadDataList (reqs : GcsPutRequestList): UploadedPaths
    fun deleteDataList (req: GcsDeleteRequestList): UploadedPaths

}