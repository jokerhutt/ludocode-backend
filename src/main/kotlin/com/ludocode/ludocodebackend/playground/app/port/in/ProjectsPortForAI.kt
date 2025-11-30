package com.ludocode.ludocodebackend.playground.app.port.`in`

import java.util.UUID

interface ProjectsPortForAI {

    fun getFileContentById(fileId: UUID): String

}