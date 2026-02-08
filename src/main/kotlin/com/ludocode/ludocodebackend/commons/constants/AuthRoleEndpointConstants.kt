package com.ludocode.ludocodebackend.commons.constants

object AuthRoleEndpointConstants {

    @JvmField
    val ADMIN_REQUIRED = arrayOf(
        "${ApiPaths.SNAPSHOTS.BASE}/**",
        "${ApiPaths.SUBJECTS.BASE}/**"
    )

}