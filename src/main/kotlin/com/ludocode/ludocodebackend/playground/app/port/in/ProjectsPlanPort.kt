package com.ludocode.ludocodebackend.playground.app.port.`in`

import java.util.UUID

interface ProjectsPlanPort {

    fun enforcePlanLimit(userId: UUID, maxProjects: Int)

}