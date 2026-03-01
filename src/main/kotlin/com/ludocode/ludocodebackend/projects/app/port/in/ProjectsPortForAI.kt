package com.ludocode.ludocodebackend.projects.app.port.`in`

import java.util.*

interface ProjectsPortForAI {

    fun getFileContentById(fileId: UUID): String

}