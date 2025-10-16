package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

class Lesson (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "course_id")
    var unitId: Int? = null,

    @Column(name = "order_index")
    var orderIndex: Int? = null,

    @Column(name = "lesson_type")
    var lessonType: Int? = null,

)