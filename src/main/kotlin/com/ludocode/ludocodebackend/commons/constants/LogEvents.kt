package com.ludocode.ludocodebackend.commons.constants

object LogEvents {

    const val AI_CREDITS_INITIALIZED = "ai_credits_initialized"
    const val AI_CREDITS_OVERDRAW_ATTEMPT = "ai_credits_overflow_attempt"
    const val AI_CREDITS_EXHAUSTED = "ai_credits_exhausted"
    const val AI_CREDITS_ADJUSTED = "ai_credits_adjusted"
    const val AI_STREAM_STARTED = "ai_stream_started"
    const val AI_STREAM_COMPLETED = "ai_stream_completed"
    const val AI_STREAM_FAILED = "ai_stream_failed"


    const val AUTH_FIREBASE_VERIFIED = "auth_firebase_verified"
    const val AUTH_FIREBASE_FAILED = "auth_firebase_failed"
    const val AUTH_LOGIN_SUCCESS = "auth_login_success"
    const val AUTH_DEMO_LOGIN_REQUESTED = "auth_demo_login_requested"

    const val AUTH_JWT_INVALID = "auth_jwt_invalid"

    const val GCS_GET_FAILED = "gcs_get_failed"
    const val STORAGE_DELETE_FAILED = "storage_delete_failed"
    const val STORAGE_UPLOAD_FAILED = "storage_upload_failed"

    const val RUNNER_EXECUTE_FAILED = "runner_execute_failed"
    const val RUNNER_EXECUTE_NONZERO_EXIT = "runner_execute_nonzero_exit"
    const val PISTON_EMPTY_RESPONSE = "piston_empty_response"
    const val PISTON_EXECUTE_FAILED = "piston_execute_failed"
    const val PISTON_RUNTIMES_FAILED = "piston_runtimes_failed"

    const val LESSON_COMPLETION_DUPLICATE = "lesson_completion_duplicate"
    const val LESSON_COMPLETION_SUBMITTED = "lesson_completion_submitted"


    const val COURSE_CREATED = "course_created"

    const val API_EXCEPTION = "api_exception"
    const val ACCESS_DENIED = "access_denied"
    const val UNHANDLED_EXCEPTION = "unhandled_exception"
    const val UNAUTHORIZED = "unauthorized"
    const val VALIDATION_FAILED = "validation_failed"

    const val COURSE_SNAPSHOT_APPLY = "course_snapshot_applied"
    const val COURSE_SNAPSHOT_BUILT = "course_snapshot_built"

    const val LESSON_EXERCISES_LOADED = "lesson_exercises_loaded"
    const val COURSE_TREE_LOADED = "course_tree_loaded"

    const val USER_ONBOARDED = "userOnboarded"
    const val USER_DELETED = "userDeleted"

    const val STREAK_GOAL_ALREADY_MET = "streak_goal_already_met"
    const val STREAK_GOAL_MET_RECORDED = "streak_goal_met_recorded"
    const val STREAK_INITIALIZED = "streak_initialized"
    const val STREAK_RESET_MISSED_DAY = "streak_reset_missed_day"
    const val STREAK_UPDATED = "streak_updated"



    const val PROJECT_SNAPSHOT_LOADED = "project_snapshot_loaded"
    const val PROJECT_RENAME_REQUESTED = "project_rename_requested"
    const val PROJECT_SNAPSHOT_DIFF = "project_snapshot_diff"
    const val PROJECT_SNAPSHOT_LIST_LOADED = "project_snapshot_list_loaded"
    const val PROJECT_DELETE_REQUESTED = "project_delete_requested"
    const val PROJECT_CREATED = "project_created"
    const val PROJECT_SNAPSHOT_FORBIDDEN = "project_snapshot_forbidden"

    const val USER_COINS_ADJUSTED = "user_coins_adjusted"
    const val USERNAME_CHANGED = "username_changed"

    const val USER_LOGIN_EXISTING = "user_login_existing"
    const val USER_CREATED = "user_created"
    const val USER_AVATAR_CHANGED = "user_avatar_changed"


}