package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "course")
class Course(

    @Id
    val id: UUID,

    @Column(name = "title", nullable = false, unique = true)
    val title: String,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "course_type", nullable = false, unique = false)
    val courseType: CourseType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id", nullable = true)
    val language: CodeLanguages?,

    @Column(name = "request_hash")
    val requestHash: UUID = UUID.randomUUID()

)
