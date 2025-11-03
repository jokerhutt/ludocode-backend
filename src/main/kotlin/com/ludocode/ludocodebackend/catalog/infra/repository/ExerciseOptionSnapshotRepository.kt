package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExerciseOptionSnapshotRepository: JpaRepository<ExerciseOption, UUID> {
}