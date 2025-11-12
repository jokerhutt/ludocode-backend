package com.ludocode.ludocodebackend.gcs.app.service

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.port.GcsUseCase
import org.springframework.stereotype.Service

@Service
class GcsService(private val storage: Storage) : GcsUseCase {

    fun uploadData (request: GcsPutRequest) : String {

        val blobId = BlobId.of(request.bucketName, request.path)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("text/plain")
            .build()

        storage.create(blobInfo, request.content.toByteArray(Charsets.UTF_8))
        return "File uploaded to GCP"

    }

    override fun getContentFromUrls (bucket: String, paths: List<String>) : Map<String, String> {
        val blobs = storage.get(paths.map { it -> BlobId.of(bucket, it)})
        return blobs.mapIndexedNotNull { i, blob ->
            blob?.let { paths[i] to it.getContent().toString(Charsets.UTF_8) }
        }.toMap()

    }



}