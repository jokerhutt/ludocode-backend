package com.ludocode.ludocodebackend.subscription.configuration

enum class Feature {
    CORE_COURSES,
    CODE_EDITOR,
    PUBLISH_PROJECTS,
    SKILL_PATHS,
    AI_ASSISTANT,
    PRIORITY_SUPPORT
}

data class PlanLimits(
    val monthlyAiCredits: Int,
    val maxProjects: Int
)

data class PlanConfig(
    val limits: PlanLimits,
    val features: Set<Feature>,
    val recommended: Boolean? = false
)
