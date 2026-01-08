package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatModule
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class CourseTreeIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getCourseTree_returnsCourseTree () {
        val user: User = user1
        val courseId: UUID = pythonId
        val response: FlatCourseTreeResponse = submitGetCourseTree(courseId, user.id!!)
        assertThat(response.modules).isNotEmpty()
        for (module: FlatModule in response.modules) {
            assertThat(module.lessons).isNotEmpty()
        }
    }

    private fun submitGetCourseTree (courseId: UUID, userId: UUID): FlatCourseTreeResponse =
        TestRestClient.getOk(ApiPaths.CATALOG.courseTree(courseId), userId, FlatCourseTreeResponse::class.java)


}