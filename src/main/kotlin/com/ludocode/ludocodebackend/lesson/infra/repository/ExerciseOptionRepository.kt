package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.ExerciseOption
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExerciseOptionRepository: JpaRepository<ExerciseOption, UUID> {

}