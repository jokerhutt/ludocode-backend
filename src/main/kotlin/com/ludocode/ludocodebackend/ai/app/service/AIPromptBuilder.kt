package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.exercise.ClozeInteraction
import com.ludocode.ludocodebackend.exercise.LExercise
import com.ludocode.ludocodebackend.exercise.SelectInteraction
import org.springframework.stereotype.Component

@Component
class AIPromptBuilder {




    internal fun buildLessonPrompt(
        req: String,
        exercise: LExercise,
        chatHistory: List<String>
    ): String {

        val filled = buildExerciseAnswerString(exercise)

        val extra = """
        Exercise context:
        ${exercise.blocks.joinToString("\n")}

        Here is the correct answer (for reasoning only — never reveal directly):
        $filled
    """.trimIndent()

        return buildBasePrompt(
            systemRole = """
            You are Ludo, a direct and concise coding assistant inside a structured lesson.

            - Never reveal full solutions unless explicitly requested.
            - No filler language.
            - If incorrect: state exactly what is wrong.
            - If correct: add one short conceptual insight.
            - Keep responses under 3 sentences unless code is necessary.
        """.trimIndent(),
            req = req,
            chatHistory = chatHistory,
            extra = extra
        )
    }

    internal fun buildGenericPrompt(
        req: String,
        chatHistory: List<String>
    ): String {

        return buildBasePrompt(
            systemRole = "You are a helpful and concise coding helper.",
            req = req,
            chatHistory = chatHistory,
        )
    }

    internal fun buildProjectPrompt(
        req: String,
        fileContent: String,
        chatHistory: List<String>
    ): String {

        val extra = """
        File context:
        $fileContent

        Ensure code blocks follow Vercel AI SDK formatting.
    """.trimIndent()

        return buildBasePrompt(
            systemRole = """
                You are Ludo, a focused coding assistant inside an in-browser code editor.
                
                - Only answer development-related questions about the current code context.
                - Be concise and direct.
                - Limit responses to 1–3 sentences.
                - Include at most one code block when necessary.
            """.trimIndent(),
            req = req,
            chatHistory = chatHistory,
            extra = extra
        )
    }

    private fun buildExerciseAnswerString(exercise: LExercise): String {

        return when (val interaction = exercise.interaction) {

            null -> buildInfoAnswer()

            is SelectInteraction ->
                buildSelectAnswer(interaction)

            is ClozeInteraction ->
                buildClozeAnswer(interaction)
        }
    }

    private fun buildBasePrompt(
        systemRole: String,
        req: String,
        chatHistory: List<String>,
        extra: String = ""
    ): String {
        val history = if (chatHistory.isEmpty()) "(no prior messages)"
        else chatHistory.joinToString("\n")

        return """
        $systemRole

        User request:
        $req

        Chat history so far:
        $history

        $extra
    """.trimIndent()
    }

    private fun buildInfoAnswer(): String {
        return "This is an informational exercise with no answers. If the user is confused about what to do, instruct them to simply press continue. If the user asks about the content, refer to the title"
    }

    private fun buildClozeAnswer(interaction: ClozeInteraction): String {

        val correctValues = interaction.blanks.map { it.correctOptions.first() }

        val filled = fillBlanks(
            interaction.file.content,
            correctValues
        )

        return "Correct solution: $filled"
    }

    private fun buildSelectAnswer(interaction: SelectInteraction): String {
        return "Correct answer: ${interaction.correctValue}"
    }

    private fun fillBlanks(
        prompt: String,
        correct: List<String>
    ): String {

        var result = prompt

        correct.forEach { value ->
            result = result.replaceFirst("___", value)
        }

        return result
    }


}