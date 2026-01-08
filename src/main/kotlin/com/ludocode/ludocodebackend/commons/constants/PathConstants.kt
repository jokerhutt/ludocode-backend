package com.ludocode.ludocodebackend.commons.constants

object PathConstants {

    const val API_V1 : String = "/api/v1";

    // == BASE == //
    const val PROJECT : String = "$API_V1/projects"
    const val PROGRESS : String = "$API_V1/progress"
    const val RUNNER : String = "$API_V1/runner"

    // == PROJECT == //
    const val GET_PROJECT : String = "/{pid}/get"
    const val GET_MY_PROJECTS : String = "/my"
    const val SAVE_PROJECT : String = "/save"
    const val DELETE_PROJECT : String = "/{pid}/delete"
    const val CREATE_PROJECT : String = "/create"
    const val RENAME_PROJECT : String = "/rename"


    // == USER == //
    const val USERS : String = "$API_V1/users"
    const val USERS_FROM_IDS : String = "/ids"
    const val PREFERENCES : String = "/preferences"
    const val SUBMIT_ONBOARDING : String = "/onboarding/submit"
    const val DELETE_USER : String = "/delete"
    const val CHANGE_AVATAR : String = "/avatar/change"

    const val PROGRESS_STREAK : String = "$PROGRESS/streak"
    const val GET_STREAK : String = "/get"
    const val GET_STREAK_WEEK : String = "/get/week"




}