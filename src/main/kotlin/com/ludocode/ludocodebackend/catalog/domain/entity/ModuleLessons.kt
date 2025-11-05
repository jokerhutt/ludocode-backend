package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "module_lessons")
class ModuleLessons (

    @EmbeddedId
    val moduleLessonsId: ModuleLessonsId,

    @Column(name = "lesson_id", nullable = false)
    val lessonId: UUID

)