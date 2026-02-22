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

    private val SUPPORTER = PlanConfig(
        limits = PlanLimits(
            monthlyAiCredits = 100,
            maxProjects = 30
        ),
        features = setOf(
            Feature.AI_ASSISTANT,
            Feature.CORE_COURSES,
            Feature.PUBLISH_PROJECTS,
            Feature.CODE_EDITOR,
            Feature.SKILL_PATHS,
            Feature.PRIORITY_SUPPORT
        ),
        recommended = true
    )

    private val DEV = PlanConfig(
        limits = PlanLimits(
            monthlyAiCredits = 1000,
            maxProjects = 300
        ),
        features = setOf(
            Feature.AI_ASSISTANT,
            Feature.CORE_COURSES,
            Feature.PUBLISH_PROJECTS,
            Feature.CODE_EDITOR,
            Feature.SKILL_PATHS,
            Feature.PRIORITY_SUPPORT
        ),
    )

    fun configFor(plan: Plan): PlanConfig =
        when (plan) {
            Plan.FREE -> FREE
            Plan.SUPPORTER -> SUPPORTER
            Plan.DEV -> DEV
        }
}
