package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserXp
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserXpRepository : JpaRepository<UserXp, UUID> {

}