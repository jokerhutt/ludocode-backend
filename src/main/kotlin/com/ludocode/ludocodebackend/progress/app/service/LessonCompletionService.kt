package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class LessonCompletionService {

    fun submitLessonCompletion (request: LessonSubmissionRequest, userId: UUID) : LessonCompletionResponse {





        //Save to lesson submission

        //Save to exercise attempt (ex id, score)

        //Save to attempt_option


    }



}