package com.ludocode.ludocodebackend.projects.app.util

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import java.util.UUID

object ProjectSnapshotValidator {


    fun validateSnapshotRequest(entryFileId: UUID, incoming: List<ProjectFileSnapshot>) {

        if (incoming.isEmpty()) throw ApiException(ErrorCode.EMPTY_REQUEST)
        if (incoming.size != incoming.map { it.path }.toSet().size) throw ApiException(ErrorCode.DUPLICATE_FILE_NAME)

        val incomingIds = incoming.mapNotNull { it.id }.toSet()

        if (entryFileId !in incomingIds)
            throw ApiException(ErrorCode.ENTRY_FILE_NOT_FOUND)

        incoming.forEach { file ->
            if (file.content.length > 512_000) throw ApiException(ErrorCode.FILE_TOO_LARGE)
        }

    }

}