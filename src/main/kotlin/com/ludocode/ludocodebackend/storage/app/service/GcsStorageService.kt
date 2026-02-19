package com.ludocode.ludocodebackend.storage.app.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageGetRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.StorageContentMap
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices


class GcsStorageService(private val storage: Storage, private val bucketName: String) : StoragePortForServices {

    override fun uploadList(req: StoragePutRequestList): UploadedPaths {
        val uploaded = mutableListOf<String>()
        try {
            req.requests.forEach { req ->
                uploadData(bucketName, req)
                uploaded.add(req.path)
            }
        } catch (ex: Exception) {
            rollbackAdditions(bucketName, uploaded)
            throw ex
        }

        return UploadedPaths(uploaded)
    }

    override fun get(path: String): String {
        val blob = storage.get(bucketName, path)
            ?: throw ApiException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, "Missing GCS object: $path")

        return String(blob.getContent(), Charsets.UTF_8)
    }

    override fun getList(req: StorageGetRequest): StorageContentMap {
        val paths = req.paths
        val result = mutableMapOf<String, String>()

        paths.forEach { path ->
            val blob = storage.get(bucketName, path)
            val text = String(blob.getContent(), Charsets.UTF_8)
            result[path] = text
        }

        return StorageContentMap(content = result)
    }

    override fun deleteList(req: StorageDeleteRequest): UploadedPaths {
        val requests = req.paths
        requests.forEach { path ->
            deleteData(bucketName, path)
        }
        return UploadedPaths(requests)
    }

    private fun uploadData(bucketName: String, request: StoragePutRequest) {

        val blobId = BlobId.of(bucketName, request.path)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("text/plain")
            .build()

        storage.create(blobInfo, request.content.toByteArray(Charsets.UTF_8))

    }

    private fun deleteData(bucketName: String, path: String): Boolean {
        return storage.delete(bucketName, path)
    }

    private fun rollbackAdditions(bucket: String, uploaded: List<String>) {
        uploaded.forEach { path -> storage.delete(bucket, path) }
    }

}
