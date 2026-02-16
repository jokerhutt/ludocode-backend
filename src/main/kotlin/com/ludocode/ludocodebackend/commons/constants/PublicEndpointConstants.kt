package com.ludocode.ludocodebackend.commons.constants
object PublicEndpointConstants {

    @JvmField
    val PUBLIC_ENDPOINTS = arrayOf(
        "/actuator/**",
        "${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.DEMO}",
        "${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.FIREBASE}",
        ApiPaths.FEATURES.BASE,
        "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.MODULES}",
        "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}",
        "${ApiPaths.CATALOG.BASE}${ApiPaths.LESSONS.EXERCISES}",
        "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSE_TREE}",
        "${ApiPaths.AI.BASE}/**",
        "${ApiPaths.SUBSCRIPTION.BASE}${ApiPaths.SUBSCRIPTION.WEBHOOK}",
        "${ApiPaths.SUBSCRIPTION.BASE}${ApiPaths.SUBSCRIPTION.WEBHOOK}/**"
    )

}