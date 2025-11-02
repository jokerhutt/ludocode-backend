package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "module_lessons")
class ModuleLessons (

    @EmbeddedId
    val moduleLessonsId: ModuleLessonsId? = null,

    @Column(name = "order_index", nullable = false, unique = true)
    val orderIndex: Int

)