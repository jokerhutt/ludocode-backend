package com.ludocode.ludocodebackend.commons.constants

import com.ludocode.ludocodebackend.commons.constants.PathConstants.AI
import com.ludocode.ludocodebackend.commons.constants.PathConstants.AUTH
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CATALOG
import com.ludocode.ludocodebackend.commons.constants.PathConstants.COURSE_TREE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.DEMO_LOGIN
import com.ludocode.ludocodebackend.commons.constants.PathConstants.LESSON_EXERCISES
import com.ludocode.ludocodebackend.commons.constants.PathConstants.GOOGLE_LOGIN
import com.ludocode.ludocodebackend.commons.constants.PathConstants.MODULES_FROM_IDS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SNAPSHOT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_FROM_IDS

object PublicEndpointConstants {

    @JvmField
    val PUBLIC_ENDPOINTS = arrayOf(
        "/actuator/**",
        AUTH + DEMO_LOGIN,
        CATALOG + MODULES_FROM_IDS,
        CATALOG,
        CATALOG + LESSON_EXERCISES,
        CATALOG + COURSE_TREE,
        USERS + USERS_FROM_IDS,
        AUTH + GOOGLE_LOGIN,
        "$AI/**",
        "$SNAPSHOT/**"

    )

}