package com.ludocode.ludocodebackend.commons.exception


import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val defaultMessage: String) {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "Lesson not found"),
    MODULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Module not found"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Project not found"),
    MODULE_NOT_FOUND_FOR_LESSON(HttpStatus.NOT_FOUND, "Module not found for given lesson"),
    LESSON_NOT_FOUND_FOR_EXERCISE(HttpStatus.NOT_FOUND, "Lesson not found for given exercise"),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Course not found"),

    NOT_ENOUGH_CREDITS(HttpStatus.UNAUTHORIZED, "Not enough credits to perform this action"),

    NOT_ALLOWED(HttpStatus.UNAUTHORIZED, "Not allowed to retrieve content"),

    GCS_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload project files to cloud storage"),

    PROJECT_FILE_ID_NULL(HttpStatus.BAD_REQUEST, "The Project File Id is null"),

    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "The file you sent exceeds the limit"),
    DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST, "Request contains duplicate file names"),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "File name is invalid"),
    EMPTY_REQUEST(HttpStatus.BAD_REQUEST, "Submission can not be empty")





}