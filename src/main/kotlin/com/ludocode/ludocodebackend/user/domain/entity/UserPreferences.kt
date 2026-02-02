package com.ludocode.ludocodebackend.user.domain.entity

import com.ludocode.ludocodebackend.user.domain.enums.DesiredPath
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "user_preferences")
class UserPreferences (

    @Id
    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "has_experience")
    val hasExperience: Boolean,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "chosen_path")
    val chosenPath: DesiredPath,

    @Column(name = "audio_enabled")
    var audioEnabled: Boolean? = true,

    @Column(name = "ai_enabled")
    var aiEnabled: Boolean? = true

)

