package com.ludocode.ludocodebackend.playground.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "project_file")
class ProjectFile (

    @Id
    val id : UUID,

    @Column(name = "project_id")
    val projectId: UUID,

    @Column(name = "name")
    val name : String,

    @Column(name = "content_url")
    val contentUrl : String

)