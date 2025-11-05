package com.ludocode.ludocodebackend.catalog.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class ModuleLessonsId (
    @Column(name = "module_id")
    var moduleId: UUID,

    @Column(name = "order_index")
    val orderIndex: Int? = null
)