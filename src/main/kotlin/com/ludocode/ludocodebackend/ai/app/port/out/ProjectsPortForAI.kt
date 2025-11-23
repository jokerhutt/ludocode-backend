package com.ludocode.ludocodebackend.ai.app.port.out

import java.util.UUID

interface ProjectsPortForAI {

    fun getFileContentById(fileId: UUID): String

}