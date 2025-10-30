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
    const val EXERCISES_LESSON_ID : String = "$EXERCISES/{lessonId}"
    const val COURSE_TREE : String = "$COURSES/{courseId}/tree"

    const val AUTH : String = "$API_V1/auth"
    const val GOOGLE_LOGIN : String = "/google-login"
    const val AUTH_ME : String = "/me"
    const val USERS : String = "$API_V1/users"
    const val USERS_IDS : String = "/ids"
    const val UPDATE_COURSE : String = "/update/course"


    const val PROGRESS : String = "$API_V1/progress"
    const val PROGRESS_COURSE : String = "$PROGRESS/course"
    const val PROGRESS_STATS : String = "$PROGRESS/stats"
    const val PROGRESS_COMPLETION : String = "$PROGRESS/completion"
    const val SUBMIT_COMPLETION : String = "/submit"
    const val RESET_PROGRESS : String = "/{courseId}/reset"
    const val STATS_BY_USER_IDS : String = "/ids"
    const val COURSE_PROGRESS_BY_COURSE_IDS : String = "/ids"
    const val ENROLLED_IDS : String = "/enrolled"


    const val SNAPSHOT : String = "$API_V1/snapshot"
    const val SUBMIT_MODULE_SNAPSHOT : String = "/submit/module"
    const val SUBMIT_COURSE_SNAPSHOT : String = "/submit"
    const val SNAPSHOTS_BY_COURSE : String = "/{courseId}"


}