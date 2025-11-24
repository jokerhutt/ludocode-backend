package com.ludocode.ludocodebackend.ai.infra.client

import com.ludocode.ludocodebackend.ai.app.port.out.CatalogPortForAI
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICATALOG
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class CatalogClientForAI(
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val baseUrl: String
) : CatalogPortForAI {

    override fun findExerciseSnapshotById(exerciseId: UUID): ExerciseSnap {
        val url = "$baseUrl$ICATALOG/$exerciseId/snapshot"
        val resp = rest.getForEntity(url, ExerciseSnap::class.java)
        return resp.body ?: error("Could not find exercise snap")
    }
}