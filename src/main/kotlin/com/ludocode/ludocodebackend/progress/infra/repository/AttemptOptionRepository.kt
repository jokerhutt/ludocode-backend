package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.AttemptOption
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.AttemptOptionId
import org.springframework.data.jpa.repository.JpaRepository

interface AttemptOptionRepository : JpaRepository<AttemptOption, AttemptOptionId>