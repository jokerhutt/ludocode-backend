package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.SubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import kotlin.test.Test


class SubjectsIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getAllSubjects_returnsList() {
        val existingSubjects = listOf(
            pythonSubject, swiftSubject
        )
        val res = submitGetAllSubjects()
        assertThat(res.map { it.subjectId })
            .containsExactlyInAnyOrderElementsOf(
                existingSubjects.map { it.id }
            )
    }

    @Test
    fun updateSubject_updatesSubject_returnsUpdatedList () {

        val existingSubjects = listOf(
            pythonSubject, swiftSubject
        )

        val subjectToChange = swiftSubject

        val req = SubjectRequest(
            name = "DSA",
            slug = "dsa",
        )

        val res = submitPutSubject(subjectToChange.id, req)

        val updated = res.firstOrNull {it.subjectId == subjectToChange.id}
            ?: fail("Updated subject not found in response")

        assertThat(updated.name).isEqualTo(req.name)
        assertThat(updated.slug).isEqualTo(req.slug)

        val courses = submitGetAllCourses()

        val coursesForUpdatedSubject = courses
            .filter { it.subject.subjectId == subjectToChange.id }

        coursesForUpdatedSubject.forEach {
            assertThat(it.subject.subjectId).isEqualTo(subjectToChange.id)
            assertThat(it.subject.name).isEqualTo(req.name)
            assertThat(it.subject.slug).isEqualTo(req.slug)
        }
    }

    @Test
    fun createSubject_usesExistingSlug_throwsError() {

        val existingSubjects = listOf(
            pythonSubject, swiftSubject
        )

        val req = SubjectRequest(
            name = "Some new subject",
            slug = pythonSubject.slug,
        )

        assertPostSubjecterror(req, ErrorCode.SLUG_EXISTS)
    }


    @Test
    fun updateSubject_usesExistingSlug_throwsError() {

        val existingSubjects = listOf(
            pythonSubject, swiftSubject
        )

        val subjectToChange = pythonSubject

        val req = SubjectRequest(
            name = subjectToChange.name,
            slug = swiftSubject.slug,
        )

        assertPutSubjectError(subjectToChange.id, req, ErrorCode.SLUG_EXISTS)
    }

    @Test
    fun createSubject_createsSubject_returnsUpdatedList () {

        val existingSubjects = listOf(pythonSubject, swiftSubject)
        val newSubjectReq = SubjectRequest(
            name = "DSA",
            slug = "dsa",
        )

        val res = submitPostSubject(newSubjectReq)
        assertThat(res.map { it.slug })
            .containsExactlyInAnyOrderElementsOf(
                existingSubjects.map { it.slug } + newSubjectReq.slug
            )
    }

    @Test
    fun deleteSubject_deletesSubject_returnsListExcludingDeleted () {
        val newSubjectReq = SubjectRequest(
            name = "DSA",
            slug = "dsa",
        )

        val existingSubjects = submitPostSubject(newSubjectReq)

        val subjectToDeleteId = existingSubjects.firstOrNull {it.slug == newSubjectReq.slug}
            ?.subjectId
            ?: fail("Created subject not found in response")

        val res = submitDeleteSubject(subjectToDeleteId)
        assertThat(res.size).isEqualTo(existingSubjects.size - 1)
        assertThat(res.map { it.subjectId })
            .doesNotContain(subjectToDeleteId)
    }

    @Test
    fun deleteSubject_hasAttachedCourse_throwsError() {
        val subjectToDeleteId = pythonSubject.id
        assertDeleteSubjectError(subjectToDeleteId, ErrorCode.SUBJECT_IN_USE)
    }


    private fun submitGetAllCourses (): Array<CourseResponse> =
        TestRestClient.getOk("${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}", user1.id, Array<CourseResponse>::class.java)

    private fun submitGetAllSubjects (): Array<CourseSubjectResponse> =
        TestRestClient.getOk(ApiPaths.SUBJECTS.BASE, userId = user1.id, Array<CourseSubjectResponse>::class.java)

    private fun assertPutSubjectError (subjectId: Long, req: SubjectRequest, statusCode: ErrorCode) : ValidatableResponse? {
        return TestRestClient.assertError("PUT", ApiPaths.SUBJECTS.bySubject(subjectId), user1.id, req, statusCode)
    }

    private fun assertPostSubjecterror (req: SubjectRequest, statusCode: ErrorCode) : ValidatableResponse? {
        return TestRestClient.assertError("POST", ApiPaths.SUBJECTS.BASE, user1.id, req, statusCode)
    }

    private fun assertDeleteSubjectError (subjectId: Long, statusCode: ErrorCode) : ValidatableResponse? {
        return TestRestClient.assertError("DELETE", ApiPaths.SUBJECTS.bySubject(subjectId), user1.id, null, statusCode)
    }

    private fun submitPutSubject (subjectId: Long, req: SubjectRequest): Array<CourseSubjectResponse> =
        TestRestClient.putOk(ApiPaths.SUBJECTS.bySubject(subjectId), user1.id, req, Array<CourseSubjectResponse>::class.java)

    private fun submitPostSubject (req: SubjectRequest): Array<CourseSubjectResponse> =
        TestRestClient.postOk(ApiPaths.SUBJECTS.BASE, user1.id, req, Array<CourseSubjectResponse>::class.java)

    private fun submitDeleteSubject (subjectId: Long): Array<CourseSubjectResponse> =
        TestRestClient.deleteOk(ApiPaths.SUBJECTS.bySubject(subjectId), user1.id, Array<CourseSubjectResponse>::class.java)

}