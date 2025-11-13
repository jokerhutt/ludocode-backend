package com.ludocode.ludocodebackend.gcs.app.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.UploadedPaths
import com.ludocode.ludocodebackend.gcs.app.port.GcsUseCase
import org.springframework.stereotype.Service

@Service
class GcsService(private val storage: Storage) : GcsUseCase {

    override fun uploadDataList (reqs: GcsPutRequestList): UploadedPaths {
        val requests = reqs.requests
        val bucket = "ludo-file-content"
        val uploadedNames = mutableListOf<String>()

        try {
            for (request in requests) {
                uploadData(bucket, request)
                uploadedNames.add(request.path)
            }
        } catch (ex: Exception) {
            rollbackAdditions(bucket, uploadedNames)
            throw ex
        }

        return UploadedPaths(uploadedNames)

    }

    override fun deleteDataList(req: GcsDeleteRequestList): UploadedPaths {
       val requests = req.paths
       val bucket = "ludo-file-content"

       try {
           for (request in requests) {
                deleteData(bucket, request)
           }
           return UploadedPaths(requests)
       } catch (ex: Exception) {
           throw ex
       }

    }

    fun uploadData (bucketName: String, request: GcsPutRequest) : String {

        val blobId = BlobId.of(bucketName, request.path)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("text/plain")
            .build()

        storage.create(blobInfo, request.content.toByteArray(Charsets.UTF_8))
        return "File uploaded to GCP"

    }

    fun deleteData(bucketName: String, path: String): String {
        val success = storage.delete(bucketName, path)

        return if (success) {
            "File deleted from GCS"
        } else {
            "File not found or already deleted"
        }
    }

    fun rollbackAdditions (bucket: String, uploaded: List<String>) {
        uploaded.forEach { path -> storage.delete(bucket, path) }
    }

    override fun getContentFromUrls (bucket: String, paths: List<String>) : Map<String, String> {
        val blobs = storage.get(paths.map { it -> BlobId.of(bucket, it)})
        return blobs.mapIndexedNotNull { i, blob ->
            blob?.let { paths[i] to it.getContent().toString(Charsets.UTF_8) }
        }.toMap()

    }






}