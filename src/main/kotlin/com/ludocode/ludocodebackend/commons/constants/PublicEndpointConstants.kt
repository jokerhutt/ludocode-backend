package com.ludocode.ludocodebackend.commons.constants

import com.ludocode.ludocodebackend.commons.constants.PathConstants.CATALOG
import com.ludocode.ludocodebackend.commons.constants.PathConstants.COURSES_ALL
import com.ludocode.ludocodebackend.commons.constants.PathConstants.EXERCISES_LESSON_ID
import com.ludocode.ludocodebackend.commons.constants.PathConstants.MODULES_COURSE_ID
import com.ludocode.ludocodebackend.commons.constants.PathConstants.MODULES_IDS

object PublicEndpointConstants {

    @JvmField
    val PUBLIC_ENDPOINTS = arrayOf(
        "/actuator/**",
        CATALOG + MODULES_IDS,
        CATALOG + COURSES_ALL,
        CATALOG + MODULES_COURSE_ID,
        CATALOG + EXERCISES_LESSON_ID,
    )

}