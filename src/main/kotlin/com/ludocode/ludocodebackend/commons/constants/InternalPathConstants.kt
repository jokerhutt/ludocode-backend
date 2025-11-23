package com.ludocode.ludocodebackend.commons.constants

object InternalPathConstants {


    const val INTERNAL_V1: String = "/internal/v1"


    const val IUSERS: String = "$INTERNAL_V1/users"
    const val IUSERS_FIND_CREATE: String = "/find-create"
    const val IUSER_ID: String = "/{userId}"
    const val IUSER_TIMEZONE: String = "/{userId}/timezone"

    const val IPROJECTS : String = "$INTERNAL_V1/projects"
    const val IPROJECTS_FILE_CONTENT_BY_ID = "/{fileId}/content"

    const val ICATALOG: String = "$INTERNAL_V1/catalog"
    const val IFIRST_LESSON_ID: String = "/{courseId}/first"
    const val ILESSON_BY_ID: String = "/{lessonId}/{userId}/get"
    const val ILESSON_ID_TREE: String = "/{lessonId}/tree"
    const val ILESSON_MODULE_ID: String = "/{lessonId}/module"
    const val ILESSON_COURSE_ID: String = "/{lessonId}/course"
    const val INEXT_LESSON_ID: String = "/{lessonId}/next"

    const val IGCS: String = "$INTERNAL_V1/gcs"
    const val IGCS_GET_CONTENT_FROM_PATH = "/get-content-single"
    const val IGCS_GET_CONTENT_FROM_PATH_LIST = "/get-content"
    const val IGCS_UPLOAD_FILES = "/upload-files"
    const val IGCS_DELETE_FILES = "/delete-files"

    const val ICOURSEPROGRESS: String = "$INTERNAL_V1/progress/course"
    const val ICOURSEPROGRESSFINDCREATE: String = "/{courseId}/{userId}"

    const val ISTREAKPROGRESS: String = "$INTERNAL_V1/progress/streak"
    const val ISTREAKUPSERT: String = "/{userId}/upsert"

    const val ICOINSPROGRESS: String = "$INTERNAL_V1/progress/coins"
    const val ICOINSUPSERT: String = "/{userId}/upsert"




}