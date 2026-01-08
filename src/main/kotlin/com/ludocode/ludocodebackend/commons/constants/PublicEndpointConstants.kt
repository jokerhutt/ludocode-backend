package com.ludocode.ludocodebackend.commons.constants
object PublicEndpointConstants {

    @JvmField
    val PUBLIC_ENDPOINTS = arrayOf(
        "/actuator/**",
        ApiPaths.AUTH.DEMO,
        ApiPaths.AUTH.FIREBASE,
        ApiPaths.FEATURES.BASE,
        ApiPaths.CATALOG.MODULES,
        ApiPaths.CATALOG.COURSES,
        ApiPaths.CATALOG.LESSON_EXERCISES,
        ApiPaths.CATALOG.COURSE_TREE,
        "${ApiPaths.AI.BASE}/**",
        "${ApiPaths.SNAPSHOTS}/**"

    )

}