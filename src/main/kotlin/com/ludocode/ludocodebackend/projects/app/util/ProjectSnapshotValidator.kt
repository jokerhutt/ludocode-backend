package com.ludocode.ludocodebackend.projects.app.util

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import java.util.UUID

object ProjectSnapshotValidator {

    fun validateSnapshotRequest(
        entryFileId: UUID,
        incoming: List<ProjectFileSnapshot>
    ): List<ProjectFileSnapshot> {

        if (incoming.isEmpty()) {
            throw ApiException(ErrorCode.EMPTY_REQUEST)
        }

        val normalizedFiles = incoming.map { file ->
            file.copy(path = normalizePath(file.path))
        }

        if (normalizedFiles.size != normalizedFiles.map { it.path }.toSet().size) {
            throw ApiException(ErrorCode.DUPLICATE_FILE_NAME)
        }

        if (normalizedFiles.none { it.id == entryFileId }) {
            throw ApiException(ErrorCode.ENTRY_FILE_NOT_FOUND)
        }

        for (file in normalizedFiles) {
            if (file.content.length > 512_000) {
                throw ApiException(ErrorCode.FILE_TOO_LARGE)
            }
        }

        return normalizedFiles
    }

    fun normalizePath(path: String): String {
        val normalizedPath = path
            .trim()
            .replace('\\', '/')

        if (normalizedPath.isBlank() || normalizedPath.startsWith("/")) {
            throw ApiException(ErrorCode.INVALID_FILE_PATH)
        }

        val segments = normalizedPath.split('/')

        if (segments.any { segment -> segment.isBlank() || segment == "." || segment == ".." }) {
            throw ApiException(ErrorCode.INVALID_FILE_PATH)
        }

        return segments.joinToString("/")
    }
}