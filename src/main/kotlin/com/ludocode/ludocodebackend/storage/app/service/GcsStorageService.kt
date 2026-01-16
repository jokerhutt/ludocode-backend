package com.ludocode.ludocodebackend.storage.app.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageGetRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.playground.config.GcsFeatureConfig
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.response.StorageContentMap
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@ConditionalOnProperty(
    prefix = "storage.gcs",
    name = ["enabled"],
    havingValue = "true"
)
class GcsStorageService(private val storage: Storage, private val gcsConfig: GcsFeatureConfig) : StoragePortForServices {

    override fun uploadList (req: StoragePutRequestList): UploadedPaths {
        val requests = req.requests
        val bucket = gcsConfig.bucket
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

    override fun get(path: String): String {
        val bucket = gcsConfig.bucket

        val blob = storage.get(bucket, path)
            ?: return ""

        println("GOT BLOB: ${blob.getContent()}")

        return String(blob.getContent(), Charsets.UTF_8)
    }

    override fun getList(req: StorageGetRequest): StorageContentMap {
        val paths = req.paths

        val bucket = gcsConfig.bucket
        val result = mutableMapOf<String, String>()

        println("One CH")

        paths.forEach { path ->
            try {
                println("Pass")
                val blob = storage.get(bucket, path)
                if (blob != null) {
                    val text = String(blob.getContent(), Charsets.UTF_8)
                    result[path] = text
                }
            } catch (_: Exception) {
                println("Missing Content")
            }
        }

        return StorageContentMap(content = result)
    }

    override fun deleteList(req: StorageDeleteRequest): UploadedPaths {
       val requests = req.paths
        val bucket = gcsConfig.bucket

       try {
           for (request in requests) {
                deleteData(bucket, request)
           }
           return UploadedPaths(requests)
       } catch (ex: Exception) {
           throw ex
       }

    }

    private fun uploadData (bucketName: String, request: StoragePutRequest) : String {

        val blobId = BlobId.of(bucketName, request.path)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("text/plain")
            .build()

        storage.create(blobInfo, request.content.toByteArray(Charsets.UTF_8))
        return "File uploaded to GCP"

    }

    private fun deleteData(bucketName: String, path: String): String {
        val success = storage.delete(bucketName, path)

        return if (success) {
            "File deleted from GCS"
        } else {
            "File not found or already deleted"
        }
    }

    private fun rollbackAdditions (bucket: String, uploaded: List<String>) {
        uploaded.forEach { path -> storage.delete(bucket, path) }
    }

}
