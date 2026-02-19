package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "module_lessons")
class ModuleLesson(

    @EmbeddedId
    val moduleLessonsId: ModuleLessonsId,

    @Column(name = "lesson_id")
    val lessonId: UUID

)