package com.ludocode.ludocodebackend.projects.app.util

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot

object ProjectSnapshotValidator {

    fun validateSnapshotRequest(
        entryFilePath: String,
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

        val normalizedEntryFilePath = normalizePath(entryFilePath)
        if (normalizedFiles.none { it.path == normalizedEntryFilePath }) {
            throw ApiException(ErrorCode.NO_DELETE_ENTRY_FILE)
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