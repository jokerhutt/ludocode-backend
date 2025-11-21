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

    const val PLAYGROUND : String = "$API_V1/playground"
    const val PROJECT : String = "$API_V1/project"
    const val RUNNER : String = "$API_V1/runner"

    const val SAVE_PROJECT : String = "/{pid}/save"
    const val DELETE_PROJECT : String = "{pid}/delete"
    const val CREATE_PROJECT : String = "/create"
    const val RENAME_PROJECT : String = "/rename"
    const val GET_PROJECT : String = "/{pid}/get"
    const val RUN_PROJECT : String = "/run"
    const val GET_MY_PROJECTS : String = "/my-projects"

    const val AUTH : String = "$API_V1/auth"
    const val GOOGLE_LOGIN : String = "/google-login"
    const val AUTH_ME : String = "/me"
    const val USERS : String = "$API_V1/users"
    const val USERS_IDS : String = "/ids"
    const val SUBMIT_ONBOARDING : String = "/onboarding/submit"
    const val UPDATE_COURSE : String = "/update/course"

    const val PREFERENCES : String = "/preferences"



    const val PROGRESS : String = "$API_V1/progress"
    const val PROGRESS_COURSE : String = "$PROGRESS/course"
    const val PROGRESS_COINS : String = "$PROGRESS/coins"
    const val PROGRESS_COMPLETION : String = "$PROGRESS/completion"
    const val SUBMIT_COMPLETION : String = "/submit"
    const val RESET_PROGRESS : String = "/{courseId}/reset"
    const val COINS_BY_USER_IDS : String = "/ids"
    const val COURSE_PROGRESS_BY_COURSE_IDS : String = "/ids"
    const val CURRENT_COURSE : String = "/current"
    const val ENROLLED_IDS : String = "/enrolled"


    const val STREAK : String = "$PROGRESS/streak"
    const val GET_STREAK : String = "/get"

    const val SNAPSHOT : String = "$API_V1/snapshot"
    const val SUBMIT_MODULE_SNAPSHOT : String = "/submit/module"
    const val SUBMIT_COURSE_SNAPSHOT : String = "/submit"
    const val CREATE_COURSE : String = "/course/create"
    const val SNAPSHOTS_BY_COURSE : String = "/{courseId}"


}