package com.ludocode.ludocodebackend.commons.constants

object PathConstants {

    const val API_V1 : String = "/api/v1";


    // == CATALOG == //
    const val CATALOG : String = "$API_V1/catalog"
    const val COURSES : String = "/courses"
    const val MODULES : String = "/modules"
    const val LESSONS : String = "/lessons"
    const val EXERCISES : String = "/exercises"

    const val MODULES_IDS : String = "$MODULES/ids"
    const val LESSONS_IDS : String = "$LESSONS/ids"

    const val COURSES_ALL : String = "$COURSES/all"
    const val MODULES_COURSE_ID : String = "$MODULES/{courseId}"
    const val LESSONS_MODULE_ID : String = "$LESSONS/all/{moduleId}"
    const val EXERCISES_LESSON_ID : String = "$EXERCISES/{lessonId}"
    const val COURSE_TREE : String = "$COURSES/{courseId}/tree"



    const val AUTH : String = "$API_V1/auth"
    const val GOOGLE_LOGIN : String = "/google-login"
    const val AUTH_ME : String = "/me"


    const val USERS : String = "$API_V1/users"
    const val USERS_IDS : String = "$API_V1/ids"












}