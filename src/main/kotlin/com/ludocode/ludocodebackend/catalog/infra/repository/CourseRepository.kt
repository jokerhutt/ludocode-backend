package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CourseRepository : JpaRepository<Course, UUID> {



}