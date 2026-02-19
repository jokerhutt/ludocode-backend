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
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LocalStorageService(
    bucketName: String
) : StoragePortForServices {

    private val bucket: Path = Paths.get(bucketName).also {
        Files.createDirectories(it)
    }

    override fun uploadList(req: StoragePutRequestList): UploadedPaths {
        val uploaded = mutableListOf<String>()
        try {
            req.requests.forEach { req ->
                uploadData(req)
                uploaded += req.path
            }
        } catch (ex: Exception) {
            rollbackAdditions(uploaded)
            throw ex
        }

        return UploadedPaths(uploaded)
    }

    override fun get(path: String): String {
        val file = bucket.resolve(path)
        if (!Files.exists(file)) {
            throw ApiException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, "Missing local object: $path")
        }
        return Files.readString(file, StandardCharsets.UTF_8)
    }

    override fun getList(req: StorageGetRequest): StorageContentMap {
        val result = mutableMapOf<String, String>()

        req.paths.forEach { path ->
            val file = bucket.resolve(path)
            if (!Files.exists(file)) {
                throw ApiException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, "Missing local object: $path")
            }
            result[path] = Files.readString(file, StandardCharsets.UTF_8)
        }

        return StorageContentMap(result)
    }

    override fun deleteList(req: StorageDeleteRequest): UploadedPaths {
        val requests = req.paths
        requests.forEach { path ->
            Files.deleteIfExists(bucket.resolve(path))
        }
        return UploadedPaths(req.paths)
    }

    private fun uploadData(req: StoragePutRequest) {
        val file = bucket.resolve(req.path)
        Files.createDirectories(file.parent)
        Files.write(file, req.content.toByteArray(StandardCharsets.UTF_8))
    }

    private fun rollbackAdditions(uploaded: List<String>) {
        uploaded.forEach { path ->
            Files.deleteIfExists(bucket.resolve(path))
        }
    }
}
