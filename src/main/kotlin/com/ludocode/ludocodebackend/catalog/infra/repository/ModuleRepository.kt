package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import org.springframework.data.jpa.repository.JpaRepository

interface ModuleRepository : JpaRepository<Module, Int> {
}