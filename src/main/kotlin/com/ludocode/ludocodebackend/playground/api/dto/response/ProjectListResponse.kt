package com.ludocode.ludocodebackend.playground.api.dto.response

import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectSnapshot

data class ProjectListResponse(val projects: List<ProjectSnapshot>)
