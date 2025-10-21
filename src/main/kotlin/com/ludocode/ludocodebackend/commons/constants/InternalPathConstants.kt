package com.ludocode.ludocodebackend.commons.constants

object InternalPathConstants {


    const val INTERNAL_V1: String = "/internal/v1"


    const val IUSERS: String = "$INTERNAL_V1/users"
    const val IUSERS_FIND_CREATE: String = "/find-create"
    const val IUSER_ID: String = "/{userId}"


    const val ICATALOG: String = "$INTERNAL_V1/catalog"
    const val IFIRST_LESSON_ID: String = "/{courseId}/first"




}