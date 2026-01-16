package com.ludocode.ludocodebackend.commons.constants

object LogEvents {

    const val GCS_GET_FAILED = "gcs_get_failed"
    const val STORAGE_DELETE_FAILED = "storage_delete_failed"
    const val STORAGE_UPLOAD_FAILED = "storage_upload_failed"

    const val RUNNER_EXECUTE_FAILED = "runner_execute_failed"
    const val RUNNER_EXECUTE_NONZERO_EXIT = "runner_execute_nonzero_exit"

    const val PROJECT_SNAPSHOT_LOADED = "project_snapshot_loaded"
    const val PROJECT_RENAME_REQUESTED = "project_rename_requested"
    const val PROJECT_SNAPSHOT_DIFF = "project_snapshot_diff"
    const val PROJECT_SNAPSHOT_LIST_LOADED = "project_snapshot_list_loaded"
    const val PROJECT_DELETE_REQUESTED = "project_delete_requested"
    const val PROJECT_CREATED = "project_created"
    const val PROJECT_SNAPSHOT_FORBIDDEN = "project_snapshot_forbidden"

}