package com.ludocode.ludocodebackend.lesson.domain.entity

import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "lesson")
class Lesson(

    @Id
    var id: UUID,

    @Column(name = "title")
    var title: String,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type")
    var lessonType: LessonType

)