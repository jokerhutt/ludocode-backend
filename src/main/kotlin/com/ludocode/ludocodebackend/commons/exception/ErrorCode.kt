package com.ludocode.ludocodebackend.commons.exception


import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val defaultMessage: String) {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "Lesson not found"),
    MODULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Module not found"),
    MODULE_NOT_FOUND_FOR_LESSON(HttpStatus.NOT_FOUND, "Module not found for given lesson"),
    LESSON_NOT_FOUND_FOR_EXERCISE(HttpStatus.NOT_FOUND, "Lesson not found for given exercise"),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Course not found"),
}