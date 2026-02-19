package com.ludocode.ludocodebackend.subscription.configuration

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan

object PlanDefinitions {

    private val FREE = PlanConfig(
        limits = PlanLimits(
            monthlyAiCredits = 5,
            maxProjects = 3
        ),
        features = setOf(
            Feature.AI_ASSISTANT,
            Feature.CORE_COURSES,
            Feature.PUBLISH_PROJECTS,
            Feature.CODE_EDITOR
        )
    )

    private val CORE = PlanConfig(
        limits = PlanLimits(
            monthlyAiCredits = 100,
            maxProjects = 10
        ),
        features = setOf(
            Feature.AI_ASSISTANT,
            Feature.CORE_COURSES,
            Feature.PUBLISH_PROJECTS,
            Feature.CODE_EDITOR,
            Feature.SKILL_PATHS,
        ),
        recommended = true
    )

    private val PRO = PlanConfig(
        limits = PlanLimits(
            monthlyAiCredits = 800,
            maxProjects = 100
        ),
        features = setOf(
            Feature.AI_ASSISTANT,
            Feature.CORE_COURSES,
            Feature.PUBLISH_PROJECTS,
            Feature.CODE_EDITOR,
            Feature.SKILL_PATHS,
            Feature.PRIORITY_SUPPORT
        )
    )

    fun configFor(plan: Plan): PlanConfig =
        when (plan) {
            Plan.FREE -> FREE
            Plan.CORE -> CORE
            Plan.PRO -> PRO
        }
}
