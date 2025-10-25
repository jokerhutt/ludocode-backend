package com.ludocode.ludocodebackend.commons.constants

object InternalPathConstants {


    const val INTERNAL_V1: String = "/internal/v1"


    const val IUSERS: String = "$INTERNAL_V1/users"
    const val IUSERS_FIND_CREATE: String = "/find-create"
    const val IUSER_ID: String = "/{userId}"


    const val ICATALOG: String = "$INTERNAL_V1/catalog"
    const val IFIRST_LESSON_ID: String = "/{courseId}/first"
    const val ILESSON_BY_ID: String = "/{lessonId}/{userId}/get"
    const val ILESSON_ID_TREE: String = "/{lessonId}/tree"
    const val ILESSON_MODULE_ID: String = "/{lessonId}/module"
    const val ILESSON_COURSE_ID: String = "/{lessonId}/course"
    const val INEXT_LESSON_ID: String = "/{lessonId}/next"

    const val ICOURSEPROGRESS: String = "$INTERNAL_V1/progress/course"
    const val ICOURSEPROGRESSFINDCREATE: String = "/{courseId}/{userId}"

    const val ISTATSPROGRESS: String = "$INTERNAL_V1/progress/stats"
    const val ISTATSUPSERT: String = "/{userId}/upsert"




}