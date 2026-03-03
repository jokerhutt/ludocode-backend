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
        const val BY_COURSE = "/{courseId}"
        const val COURSE_SUBJECT = "$BY_COURSE/subject"
        const val COURSE_LANGUAGE = "$BY_COURSE/language"
        const val CURRICULUM = "/curriculum"
        const val BY_COURSE_CURRICULUM = "/curriculum/{courseId}"
        fun byCourse(courseId: UUID): String = "$BASE/$courseId"
        fun byCourseCurriculum(courseId: UUID): String = "$BASE$CURRICULUM$BY_COURSE"
        fun byCourseCurriculumAdmin(courseId: UUID): String = "$ADMIN_BASE$CURRICULUM/$courseId"
        fun byLessonCurriculumAdmin(lessonId: UUID): String = "$ADMIN_BASE$CURRICULUM/lesson/$lessonId"
        fun byCourseAdmin(courseId: UUID): String = "$ADMIN_BASE/$courseId"
    }

    object SUBJECTS {
        const val BASE = "$API_PREFIX/subjects"
        const val ADMIN_BASE = "$API_PREFIX$ADMIN_PREFIX/subjects"
        const val BY_SUBJECT = "/{subjectId}"
        fun bySubject(subjectId: Long): String = "$BASE/$subjectId"
        fun bySubjectAdmin(subjectId: Long): String = "$ADMIN_BASE/$subjectId"
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