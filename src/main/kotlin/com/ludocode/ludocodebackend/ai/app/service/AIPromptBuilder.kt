package com.ludocode.ludocodebackend.ai.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import org.springframework.stereotype.Component

@Component
class AIPromptBuilder {


    internal fun buildLessonPrompt(req: String, exercise: ExerciseSnap): String {

        val filled = buildExerciseAnswerString(exerciseSnap = exercise)
        println("FILLED: " + filled)

        return """
        You are a helpful coding helper.
        The user is currently doing an exercise titled: ${exercise.title}

        User request: $req

        Here is the correct answer if applicable, use it only for yourself and do not give the user the answer directly.
        $filled

        You may use hints when answering, but let the user figure out the solution if they are asking for help.
    """.trimIndent()
    }

    internal fun buildGenericPrompt (req: String) =
        """ 
        You are a helpful and concise coding helper on a code learning app.
        The user asks: ${req}
        
        Respond with:
        - A fitting answer to their request
        - Hints
        - Clarification if required
        """.trimIndent()

    internal fun buildProjectPrompt(req: String, fileContent: String): String =
        """
        You are a helpful and concise coding helper on a code learning app.
        The user asks: ${req}
        
        Their current file context is: ${fileContent}.
        
        Ensure that any code markdown is formatted according to the Vercel AI SDK dev requirements.

        Respond with:
        - A fitting answer to their request
        - Hints
        - Fixed code if required
        """.trimIndent()

    private fun buildExerciseAnswerString(exerciseSnap: ExerciseSnap) : String {
        val hasPrompt = exerciseSnap.prompt != null
        val hasCorrectOptions = exerciseSnap.correctOptions != null

        if (exerciseSnap.exerciseType == ExerciseType.INFO) {
            return buildInfoAnswer()
        }

        if (!hasCorrectOptions) return ""

        if (exerciseSnap.exerciseType == ExerciseType.TRIVIA) {
            return buildTriviaAnswer(exerciseSnap)
        }

        if (!hasPrompt) return ""


        if (exerciseSnap.exerciseType == ExerciseType.ANALYZE) {
           return buildAnalyzeAnswer(exerciseSnap)
        }

        if (exerciseSnap.exerciseType == ExerciseType.CLOZE) {
            return buildClozeAnswer(exerciseSnap)
        }

        return ""

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