package com.ludocode.ludocodebackend.gcs.app.port

import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.UploadedPaths

interface GcsUseCase {

    fun getContentFromUrls (bucket: String, paths: List<String>): Map<String, String>
    fun uploadDataList (reqs : GcsPutRequestList): UploadedPaths
    fun deleteDataList (req: GcsDeleteRequestList): UploadedPaths

}