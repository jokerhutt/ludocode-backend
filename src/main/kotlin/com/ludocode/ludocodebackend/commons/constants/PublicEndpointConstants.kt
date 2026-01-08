package com.ludocode.ludocodebackend.commons.constants
import com.ludocode.ludocodebackend.commons.constants.PathConstants.FEATURES
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_FROM_IDS

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
        USERS + USERS_FROM_IDS,
        "${ApiPaths.AI.BASE}/**",
        "${ApiPaths.SNAPSHOTS}/**"

    )

}