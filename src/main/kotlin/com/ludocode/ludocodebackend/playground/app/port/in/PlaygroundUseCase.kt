package com.ludocode.ludocodebackend.playground.app.port.`in`

import java.util.UUID

interface PlaygroundUseCase {
    fun getFileContentById(fileId: UUID): String
}