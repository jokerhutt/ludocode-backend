package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository

interface LessonRepository : JpaRepository<Lesson, Int> {
}