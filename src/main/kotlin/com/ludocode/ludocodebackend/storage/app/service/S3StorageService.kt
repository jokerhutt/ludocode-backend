package com.ludocode.ludocodebackend.storage.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageGetRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.StorageContentMap
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.charset.StandardCharsets

class S3StorageService(private val s3Client: S3Client, private val bucketName: String) : StoragePortForServices {
    override fun uploadList(req: StoragePutRequestList): UploadedPaths {
        val uploaded = mutableListOf<String>()

        try {
            req.requests.forEach { putReq ->
                uploadData(putReq)
                uploaded += putReq.path
            }
        } catch (ex: Exception) {
            rollbackAdditions(uploaded)
            throw ex
        }

        return UploadedPaths(uploaded)
    }

    override fun get(path: String): String {
        return try {
            val bytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build()
            ).asByteArray()

            String(bytes, StandardCharsets.UTF_8)
        } catch (ex: NoSuchKeyException) {
            throw ApiException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, "Missing S3 object: $path")
        }
    }

    override fun getList(req: StorageGetRequest): StorageContentMap {
        val result = mutableMapOf<String, String>()

        req.paths.forEach { path ->
            result[path] = get(path)
        }

        return StorageContentMap(content = result)
    }

    override fun deleteList(req: StorageDeleteRequest): UploadedPaths {
        req.paths.forEach { path ->
            deleteData(path)
        }
        return UploadedPaths(req.paths)
    }

    private fun uploadData(req: StoragePutRequest) {
        val putReq = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(req.path)
            .contentType("text/plain")
            .build()

        s3Client.putObject(
            putReq,
            RequestBody.fromBytes(req.content.toByteArray(StandardCharsets.UTF_8))
        )
    }

    private fun deleteData(path: String) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build()
        )
    }

    private fun rollbackAdditions(uploaded: List<String>) {
        uploaded.forEach { path ->
            runCatching { deleteData(path) }
        }
    }
}