package com.ludocode.ludocodebackend.commons.constants

import java.util.*

object ApiPaths {

    const val API_PREFIX = "/api/v1"
    const val ADMIN_PREFIX = "/admin"

    object AI {
        const val BASE = "$API_PREFIX/ai"
        const val COMPLETIONS = "/completions"
    }

    object AUTH {
        const val BASE = "$API_PREFIX/auth"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/auth"
        const val CHECK = "/check"
        const val FIREBASE = "/firebase"
        const val ME = "/me"
        const val LOGOUT = "/logout"
    }

    object SUBSCRIPTION {
        const val BASE = "$API_PREFIX/subscription"
        const val PLANS = "/plans"
        const val CONFIRM = "/confirm"
        const val CHECKOUT = "/checkout"
        const val WEBHOOK = "/webhook"
        const val MANAGE = "/manage"
    }

    object CATALOG {
        const val BASE = "$API_PREFIX/catalog"
        const val COURSES = "/courses"
        const val COURSE_TREE = "/courses/{courseId}/tree"
        const val MODULES = "/modules"

        fun courseTree(courseId: UUID): String = "$BASE$COURSES/$courseId/tree"

    }

    object CREDITS {
        const val BASE = "$API_PREFIX/credits"
    }

    object FEATURES {
        const val BASE = "$API_PREFIX/features"
    }

    object LANGUAGES {
        const val BASE = "$API_PREFIX/languages"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/languages"
        const val ID = "/{id}"
        fun byId(id: Long): String = "$BASE/$id"
        fun byIdAdmin(id: Long): String = "$ADMIN_BASE/$id"
    }

    object LESSONS {
        const val BASE = "$API_PREFIX/lessons"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/lessons"
        const val BY_ID = "/{lessonId}"
        const val EXERCISES = "$BY_ID/exercises"
        fun byId(id: UUID): String = "$BASE/$id"
        fun byAdminId(id: UUID): String = "$ADMIN_BASE/$id"
        fun byIdExercises(id: UUID): String = "$BASE/$id/exercises"
    }

    object PROGRESS {
        const val BASE = "$API_PREFIX/progress"

        object COINS {
            const val BASE = "${PROGRESS.BASE}/coins"
        }

        object COMPLETION {
            const val BASE = "${PROGRESS.BASE}/completion"
        }

        object COURSES {
            const val BASE = "${PROGRESS.BASE}/courses"
            const val ENROLLED = "/enrolled"
            const val CURRENT = "/current"
            const val STATS = "/stats"
            const val RESET = "/{courseId}/reset"
            fun reset(courseId: UUID): String = "$BASE/$courseId/reset"
        }

        object STREAK {
            const val BASE = "${PROGRESS.BASE}/streak"
            fun weekly() = "$BASE?mode=weekly"
        }

    }

    object PROJECTS {
        const val BASE = "$API_PREFIX/projects"
        const val BY_ID = "/{projectId}"
        const val LANGUAGES = "/languages"
        const val NAME = "/{projectId}/name"
        fun byId(projectId: UUID): String = "$BASE/$projectId"
        fun name(projectId: UUID): String = "$BASE/$projectId/name"
    }

    object RUNNER {
        const val BASE = "$API_PREFIX/runner"
        const val EXECUTE = "/executions"
    }

    object SNAPSHOTS {
        const val BASE = "$API_PREFIX/snapshots"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/snapshots"
        const val COURSE = "/course"
        const val COURSES = "/courses"
        const val BY_COURSE = "/{courseId}"
        const val BY_COURSE_STATUS = "$BY_COURSE/status"
        const val COURSE_TAG = "$BY_COURSE/tag"
        const val COURSE_LANGUAGE = "$BY_COURSE/language"
        const val COURSE_ICON = "$BY_COURSE/icon"
        const val CURRICULUM = "/curriculum"
        const val BY_COURSE_CURRICULUM = "/curriculum/{courseId}"
        fun byCourse(courseId: UUID): String = "$BASE/$courseId"
        fun byCourseCurriculum(courseId: UUID): String = "$BASE$CURRICULUM$BY_COURSE"
        fun byCourseAdminStatus(courseId: UUID): String = "$ADMIN_BASE/$courseId/visibility"
        fun byCourseCurriculumAdmin(courseId: UUID): String = "$ADMIN_BASE$CURRICULUM/$courseId"
        fun byLessonCurriculumAdmin(lessonId: UUID): String = "$ADMIN_BASE$CURRICULUM/lesson/$lessonId"
        fun byCourseAdmin(courseId: UUID): String = "$ADMIN_BASE/$courseId"
    }

    object TAGS {
        const val BASE = "$API_PREFIX/tags"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/tags"
        const val BY_TAG = "/{tagId}"
        fun bySubject(tagId: Long): String = "$BASE/$tagId"
        fun byTagAdmin(tagId: Long): String = "$ADMIN_BASE/$tagId"
    }

    object PREFERENCES {
        const val BASE = "$API_PREFIX/preferences"
        const val CAREERS = "/careers"
    }

    object USERS {
        const val BASE = "$API_PREFIX/users"
        const val ME = "/me"
        const val AVATAR = "/me/avatar"
        fun fromIds(ids: List<UUID>): String =
            BASE + ids.joinToString(
                prefix = "?userIds=",
                separator = "&userIds="
            ) { it.toString() }
    }


}