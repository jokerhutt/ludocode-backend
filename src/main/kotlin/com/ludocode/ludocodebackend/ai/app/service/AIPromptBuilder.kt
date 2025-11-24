package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import org.springframework.stereotype.Component

@Component
class AIPromptBuilder {


    internal fun buildLessonPrompt(
        req: String,
        exercise: ExerciseSnap,
        chatHistory: List<String>
    ): String {

        val filled = buildExerciseAnswerString(exercise)

        val extra = """
        Exercise title: ${exercise.title}

        Here is the correct answer (for your own reasoning only—never reveal directly):
        $filled

        Use hints but do not give away solutions outright.
    """.trimIndent()

        return buildBasePrompt(
            systemRole = "You are a helpful coding helper for a coding-learning app.",
            req = req,
            chatHistory = chatHistory,
            extra = extra
        )
    }

    internal fun buildGenericPrompt(
        req: String,
        chatHistory: List<String>
    ): String {

        val extra = """
        Respond with:
        - A fitting answer to their request
        - Hints
        - Clarification if required
    """.trimIndent()

        return buildBasePrompt(
            systemRole = "You are a helpful and concise coding helper.",
            req = req,
            chatHistory = chatHistory,
            extra = extra
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

        Respond with:
        - A fitting answer
        - Hints
        - Any necessary fixed code
    """.trimIndent()

        return buildBasePrompt(
            systemRole = "You are a helpful and concise coding helper on a code-learning app.",
            req = req,
            chatHistory = chatHistory,
            extra = extra
        )
    }

    private fun buildExerciseAnswerString(exerciseSnap: ExerciseSnap) : String {
        val hasPrompt = exerciseSnap.prompt != null
        val hasCorrectOptions = exerciseSnap.correctOptions != null

        when (exerciseSnap.exerciseType) {
            ExerciseType.INFO -> return buildInfoAnswer()
            ExerciseType.TRIVIA -> {
                if (!hasCorrectOptions) return ""
                return buildTriviaAnswer(exerciseSnap)
            }
            ExerciseType.ANALYZE -> {
                if (!hasPrompt || !hasCorrectOptions) return ""
                return buildAnalyzeAnswer(exerciseSnap)
            }
            ExerciseType.CLOZE -> {
                if (!hasPrompt || !hasCorrectOptions) return ""
                return buildClozeAnswer(exerciseSnap)
            }
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

    private fun buildInfoAnswer () : String {
        return "This is an informational exercise with no answers. If the user is confused about what to do, instruct them to simply press continue. If the user asks about the content, refer to the title"
    }

    private fun buildClozeAnswer (exerciseSnap: ExerciseSnap): String {
        println("BLANKS: " + fillBlanks(exerciseSnap.prompt!!, exerciseSnap.correctOptions!!))
        return "The exercise's correct solution is: " + fillBlanks(exerciseSnap.prompt!!, exerciseSnap.correctOptions!!)
    }

    private fun buildTriviaAnswer (exerciseSnap: ExerciseSnap): String {
        return "The correct answer is: ${exerciseSnap.correctOptions[0].content}"
    }

    private fun buildAnalyzeAnswer (exerciseSnap: ExerciseSnap) : String {
        return "The exercise asks the user to analyse the code: ${exerciseSnap.prompt}. The answer is ${exerciseSnap.correctOptions[0]}"
    }

    private fun fillBlanks(prompt: String, correct: List<OptionSnap>): String {
        var result = prompt
        correct.forEach { answer ->
            result = result.replaceFirst("___", answer.content)
        }
        return result
    }





}