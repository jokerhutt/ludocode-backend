package com.ludocode.ludocodebackend.commons.constants

object AuthRoleEndpointConstants {

    @JvmField
    val ADMIN_REQUIRED = arrayOf(
        "${ApiPaths.API_PREFIX}${ApiPaths.ADMIN_PREFIX}/**"
    )

}