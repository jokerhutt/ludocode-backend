package com.ludocode.ludocodebackend.user.infra.repository

import com.ludocode.ludocodebackend.user.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {



}