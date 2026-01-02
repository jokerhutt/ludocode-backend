package com.ludocode.ludocodebackend.storage.app.service

import com.ludocode.ludocodebackend.storage.app.dto.request.MediaPutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.storage.configuration.LocalStorageConfig

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@ConditionalOnProperty(
    prefix = "storage.gcs",
    name = ["enabled"],
    havingValue = "false"
)
class LocalStorageService(
    config: LocalStorageConfig
) : StoragePortForServices {

    private val bucket: Path = Paths.get(config.bucketName).also {
        Files.createDirectories(it)
    }

    override fun uploadDataList(reqs: StoragePutRequestList): UploadedPaths {
        val uploaded = mutableListOf<String>()

        try {
            reqs.requests.forEach { req ->
                uploadData(req)
                uploaded += req.path
            }
        } catch (ex: Exception) {
            rollbackAdditions(uploaded)
            throw ex
        }

        return UploadedPaths(uploaded)
    }


    override fun uploadMedia(req: MediaPutRequest): String {
        val file = bucket.resolve(req.path)
        Files.createDirectories(file.parent)
        Files.write(file, req.bytes)
        return req.path
    }

    override fun getMedia(path: String): ByteArray {
        val file = bucket.resolve(path)
        return if (Files.exists(file)) Files.readAllBytes(file) else ByteArray(0)
    }

    override fun getContentFromPath(path: String): String {
        val file = bucket.resolve(path)
        return if (Files.exists(file)) {
            Files.readString(file, StandardCharsets.UTF_8)
        } else ""
    }

    override fun getContentFromUrls(paths: List<String>): Map<String, String> {
        return paths.associateWith { path ->
            try {
                val file = bucket.resolve(path)
                if (Files.exists(file)) Files.readString(
                    file,
                    StandardCharsets.UTF_8
                ) else ""
            } catch (_: Exception) {
                ""
            }
        }.filterValues { it.isNotEmpty() }
    }

    override fun deleteDataList(req: StorageDeleteRequest): UploadedPaths {
        req.paths.forEach { path ->
            Files.deleteIfExists(bucket.resolve(path))
        }
        return UploadedPaths(req.paths)
    }

    private fun uploadData(req: StoragePutRequest): String {
        val file = bucket.resolve(req.path)
        Files.createDirectories(file.parent)
        Files.write(file, req.content.toByteArray(StandardCharsets.UTF_8))
        return "File saved to local storage"
    }

    private fun rollbackAdditions(uploaded: List<String>) {
        uploaded.forEach { path ->
            Files.deleteIfExists(bucket.resolve(path))
        }
    }
}