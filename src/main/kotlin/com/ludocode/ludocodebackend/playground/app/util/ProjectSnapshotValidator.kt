package com.ludocode.ludocodebackend.playground.app.util

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import kotlin.collections.forEach

object ProjectSnapshotValidator {


    fun validateSnapshotRequest (incoming: List<ProjectFileSnapshot>) {

        if (incoming.isEmpty()) throw ApiException(ErrorCode.EMPTY_REQUEST)
        if (incoming.size != incoming.map { it.path }.toSet().size) throw ApiException(ErrorCode.DUPLICATE_FILE_NAME)

        incoming.forEach { file ->
            if (!validateFilePathRegex(file.path)) throw ApiException(ErrorCode.INVALID_FILE_NAME)
            if (file.content.length > 512_000) throw ApiException(ErrorCode.FILE_TOO_LARGE)
        }
    }

    private fun validateFilePathRegex (filePath: String) : Boolean {
        val allowed = Regex("""^[\w.-]+\.(py|swift|js|css|html)$""")
        return filePath.matches(allowed)
    }


}