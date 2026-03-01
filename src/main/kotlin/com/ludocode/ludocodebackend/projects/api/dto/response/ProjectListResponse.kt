package com.ludocode.ludocodebackend.projects.api.dto.response

import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot

data class ProjectListResponse(val projects: List<ProjectSnapshot>)
