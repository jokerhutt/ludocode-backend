package com.ludocode.ludocodebackend.playground.app.dto.response

import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot

data class ProjectListResponse(val projects: List<ProjectSnapshot>)
