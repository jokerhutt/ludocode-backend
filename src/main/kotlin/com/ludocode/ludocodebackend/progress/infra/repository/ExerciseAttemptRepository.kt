package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.ExerciseAttempt
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ExerciseAttemptRepository : JpaRepository<ExerciseAttempt, UUID>