package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.CourseTagId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table


@Entity
@Table(name = "course_tag")
class CourseTag (

    @EmbeddedId
    val courseTagId: CourseTagId

)