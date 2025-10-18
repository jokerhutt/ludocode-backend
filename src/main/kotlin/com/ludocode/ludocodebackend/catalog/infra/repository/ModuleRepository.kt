package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ModuleRepository : JpaRepository<Module, UUID> {
}