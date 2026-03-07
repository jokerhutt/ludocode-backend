package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "course")
@SQLRestriction("is_deleted = false")
class Course(

    @Id
    val id: UUID,

    @Column(name = "title", nullable = false, unique = true)
    val title: String,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "course_type", nullable = false, unique = false)
    val courseType: CourseType,

    @Column(name = "description")
    val description: String,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    @Column(name = "course_icon")
    var courseIcon: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id", nullable = true)
    var language: CodeLanguages?,

    @Column(name = "request_hash")
    val requestHash: UUID = UUID.randomUUID()

)
