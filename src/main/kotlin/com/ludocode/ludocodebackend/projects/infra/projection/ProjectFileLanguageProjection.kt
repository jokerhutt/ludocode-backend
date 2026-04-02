package com.ludocode.ludocodebackend.projects.infra.projection

import java.util.UUID

interface ProjectFileLanguageProjection {
    fun getProjectId(): UUID
    fun getCodeLanguage(): String
}

