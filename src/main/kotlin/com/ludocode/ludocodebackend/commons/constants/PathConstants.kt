package com.ludocode.ludocodebackend.commons.constants

object PathConstants {

    const val API_V1 : String = "/api/v1";

    // == BASE == //
    const val AI : String = "$API_V1/ai"
    const val CREDITS : String = "$API_V1/credits"
    const val AUTH : String = "$API_V1/auth"
    const val CATALOG : String = "$API_V1/catalog"
    const val PROJECT : String = "$API_V1/projects"
    const val PROGRESS : String = "$API_V1/progress"
    const val RUNNER : String = "$API_V1/runner"

    // == CATALOG == //
    const val FEATURES : String = "$API_V1/features"

    // == AI == //
    const val AI_SEND_PROMPT : String = "/prompt/send"


    // == PROJECT == //
    const val GET_PROJECT : String = "/{pid}/get"
    const val GET_MY_PROJECTS : String = "/my"
    const val SAVE_PROJECT : String = "/save"
    const val DELETE_PROJECT : String = "/{pid}/delete"
    const val CREATE_PROJECT : String = "/create"
    const val RENAME_PROJECT : String = "/rename"


    // == RUNNER == //
    const val RUN_PROJECT : String = "/run"

    // == AUTH == //
    const val DEMO_LOGIN : String = "/login/demo"
    const val FIREBASE_LOGIN : String = "/firebase"

    // == USER == //
    const val USERS : String = "$API_V1/users"
    const val USERS_FROM_IDS : String = "/ids"
    const val PREFERENCES : String = "/preferences"
    const val SUBMIT_ONBOARDING : String = "/onboarding/submit"
    const val DELETE_USER : String = "/delete"
    const val CHANGE_AVATAR : String = "/avatar/change"

    // == PROGRESS == //
    const val PROGRESS_COURSE : String = "$PROGRESS/course"
    const val CURRENT_COURSE : String = "/current"
    const val ENROLLED_IDS : String = "/enrolled"
    const val COURSE_PROGRESS_FROM_COURSE_IDS : String = "/ids"
    const val RESET_PROGRESS : String = "/course/{courseId}/reset"
    const val UPDATE_COURSE : String = "/course/change"

    const val PROGRESS_COMPLETION : String = "$PROGRESS/completion"
    const val SUBMIT_COMPLETION : String = "/submit"

    const val PROGRESS_STREAK : String = "$PROGRESS/streak"
    const val GET_STREAK : String = "/get"
    const val GET_STREAK_WEEK : String = "/get/week"




}