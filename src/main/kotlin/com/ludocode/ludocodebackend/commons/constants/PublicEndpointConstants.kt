package com.ludocode.ludocodebackend.commons.constants

import com.ludocode.ludocodebackend.commons.constants.PathConstants.AUTH
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CATALOG
import com.ludocode.ludocodebackend.commons.constants.PathConstants.COURSES_ALL
import com.ludocode.ludocodebackend.commons.constants.PathConstants.EXERCISES_LESSON_ID
import com.ludocode.ludocodebackend.commons.constants.PathConstants.GOOGLE_LOGIN
import com.ludocode.ludocodebackend.commons.constants.PathConstants.MODULES_COURSE_ID
import com.ludocode.ludocodebackend.commons.constants.PathConstants.MODULES_IDS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_IDS

object PublicEndpointConstants {

    @JvmField
    val PUBLIC_ENDPOINTS = arrayOf(
        "/actuator/**",
        "/internal/**",
        CATALOG + MODULES_IDS,
        CATALOG + COURSES_ALL,
        CATALOG + MODULES_COURSE_ID,
        CATALOG + EXERCISES_LESSON_ID,
        USERS + USERS_IDS,
        AUTH + GOOGLE_LOGIN

    )

}