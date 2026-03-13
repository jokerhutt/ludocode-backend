package com.ludocode.ludocodebackend.lesson.domain.entity

import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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
    var lessonType: LessonType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "files")
    var projectSnapshot: ProjectSnapshot? = null

)