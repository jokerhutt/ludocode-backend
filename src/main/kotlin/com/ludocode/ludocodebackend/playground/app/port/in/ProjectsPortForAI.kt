package com.ludocode.ludocodebackend.playground.app.port.`in`

import java.util.*

interface ProjectsPortForAI {

    fun getFileContentById(fileId: UUID): String

}