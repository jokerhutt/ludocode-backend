package com.ludocode.ludocodebackend.onboarding.api.dto

object OnboardingFormDefinition {

    const val VERSION = 1

    fun build(): OnboardingFormResponse {
        return OnboardingFormResponse(
            version = VERSION,
            title = "Welcome to Ludocode",
            fields = listOf(
                SelectFieldDef(
                    key = OnboardingKeys.CHOSEN_PATH,
                    label = "Choose your path",
                    options = listOf(
                        OptionDef(value = "DATA", label = "Data"),
                        OptionDef(value = "IOS", label = "iOS"),
                    )
                ),

                BooleanFieldDef(
                    key = OnboardingKeys.HAS_EXPERIENCE,
                    label = "Do you have programming experience?"
                ),

                TextFieldDef(
                    key = OnboardingKeys.USERNAME,
                    label = "Choose a username",
                    minLen = 3,
                    maxLen = 20,
                    regex = "^[a-zA-Z0-9_]+$"
                )

            )
        )
    }
}