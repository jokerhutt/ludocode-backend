package com.ludocode.ludocodebackend.commons.exception


import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val defaultMessage: String) {

    BAD_REQ(HttpStatus.BAD_REQUEST, "Bad Request"),
    EMAIL_IN_USE(HttpStatus.CONFLICT, "Email is already in use"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    TREE_NOT_FOUND(HttpStatus.NOT_FOUND, "Tree not found"),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "Lesson not found"),
    MODULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Module not found"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found"),
    MODULE_NOT_FOUND_FOR_LESSON(HttpStatus.NOT_FOUND, "Module not found for given lesson"),
    LESSON_NOT_FOUND_FOR_EXERCISE(HttpStatus.NOT_FOUND, "Lesson not found for given exercise"),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Course not found"),

    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Selected plan does not exist"),
    PLAN_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Plan is not active"),
    LIMITS_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not find limits for selected plan"),

    USER_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND,"User subscription not found"),

    COURSE_STATS_NOT_FOUND(HttpStatus.NOT_FOUND, "Course stats for this user and course not found"),
    COURSE_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Course progress for this user and course not found"),

    LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Language not found. Does it exist?"),
    SLUG_EXISTS(HttpStatus.BAD_REQUEST, "Slug already exists"),
    EDITOR_ID_EXISTS(HttpStatus.BAD_REQUEST, "Editor ID already exists"),
    PISTON_ID_EXISTS(HttpStatus.BAD_REQUEST, "Piston ID already exists"),

    COURSE_EXISTS(HttpStatus.BAD_REQUEST, "This course slug or name already exists, did you already create it?"),
    SUBJECT_IN_USE(HttpStatus.BAD_REQUEST, "This subject is already in use by other courses"),

    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Subject not found"),

    NOT_ENOUGH_CREDITS(HttpStatus.UNAUTHORIZED, "Not enough credits to perform this action"),

    NOT_ALLOWED(HttpStatus.UNAUTHORIZED, "Not allowed to retrieve content"),

    GCS_GET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get project files from cloud storage"),
    STORAGE_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Storage object not found"),
    GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload project files to cloud storage"),

    PROJECT_FILE_ID_NULL(HttpStatus.BAD_REQUEST, "The Project File Id is null"),
    PROJECT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not find the given project file"),

    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "The file you sent exceeds the limit"),
    DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST, "Request contains duplicate file names"),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "File name is invalid"),
    EMPTY_REQUEST(HttpStatus.BAD_REQUEST, "Submission can not be empty"),


    STRIPE_METADATA_MISSING(HttpStatus.BAD_REQUEST, "Stripe webhook metadata missing"),
    STRIPE_SUBSCRIPTION_INVALID(HttpStatus.BAD_REQUEST, "Stripe subscription invalid"),
    STRIPE_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Invalid Stripe signature"),





}