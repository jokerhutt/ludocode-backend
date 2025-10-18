package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class ModuleMapper(private val basicMapper: BasicMapper) {

    fun toModuleResponse(module: Module): ModuleResponse =
        basicMapper.one(module) {
            ModuleResponse(
                id = it.id!!,
                title = it.title!!,
                courseId = it.courseId!!,
                orderIndex = it.orderIndex!!
            )
        }

    fun toModuleResponseList(modules: List<Module>): List<ModuleResponse> =
        basicMapper.list(modules) { module ->
            toModuleResponse(module)
        }


}
