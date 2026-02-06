package com.ludocode.ludocodebackend.commons.constants

import java.util.UUID

object ApiPaths {

    const val API_PREFIX = "/api/v1"

    object AI {
        const val BASE = "$API_PREFIX/ai"
        const val COMPLETIONS = "/completions"
    }

    object AUTH {
        const val BASE = "$API_PREFIX/auth"
        const val FIREBASE = "/firebase"
        const val ME = "/me"
        const val DEMO = "/demo"
        const val LOGOUT = "/logout"
    }

    object CATALOG {
        const val BASE = "$API_PREFIX/catalog"
        const val COURSES = "/courses"
        const val COURSE_TREE = "/courses/{courseId}/tree"
        const val MODULES = "/modules"
        const val LESSONS = "/lessons"
        const val LESSON_EXERCISES = "/lessons/{lessonId}/exercises"

        fun courseTree(courseId: UUID) : String = "$BASE$COURSES/$courseId/tree"
        fun lessonExercises(lessonId: UUID) : String = "$BASE$LESSONS/$lessonId/exercises"

    }

    object CREDITS {
        const val BASE = "$API_PREFIX/credits"
    }

    object FEATURES {
        const val BASE = "$API_PREFIX/features"
    }

    object LANGUAGES {
        const val BASE = "$API_PREFIX/languages"
        const val ID = "/{id}"
        fun byId(id: Long) : String = "$BASE/$id"
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
            fun reset(courseId: UUID) : String = "$BASE/$courseId/reset"
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
        fun byId(projectId: UUID) : String = "$BASE/$projectId"
        fun name(projectId: UUID) : String = "$BASE/$projectId/name"
    }

    object RUNNER {
        const val BASE = "$API_PREFIX/runner"
        const val EXECUTE = "/executions"
    }

    object SNAPSHOTS {
        const val BASE = "$API_PREFIX/snapshots"
        const val COURSE = "/course"
        const val BY_COURSE = "/{courseId}"

        fun byCourse(courseId: UUID): String = "$BASE/$courseId"

    }

    object PREFERENCES {
        const val BASE = "$API_PREFIX/preferences"
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