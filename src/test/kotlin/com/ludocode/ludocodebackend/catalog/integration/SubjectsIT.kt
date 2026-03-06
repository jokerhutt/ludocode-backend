package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import org.junit.jupiter.api.BeforeEach


class SubjectsIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

//    @Test
//    fun getAllSubjects_returnsList() {
//        val existingSubjects = listOf(
//            pythonSubject, swiftSubject
//        )
//        val res = submitGetAllSubjects()
//        assertThat(res.map { it.id })
//            .containsExactlyInAnyOrderElementsOf(
//                existingSubjects.map { it.id }
//            )
//    }
//
//    @Test
//    fun updateSubject_updatesSubject_returnsUpdatedList() {
//
//        listOf(
//            pythonSubject, swiftSubject
//        )
//
//        val subjectToChange = swiftSubject
//
//        val req = SubjectMetadata(
//            name = "DSA",
//            slug = "dsa",
//            id = 0
//        )
//
//        val res = submitPutSubject(subjectToChange.id, req)
//
//        val updated = res.firstOrNull { it.id == subjectToChange.id }
//            ?: fail("Updated subject not found in response")
//
//        assertThat(updated.name).isEqualTo(req.name)
//        assertThat(updated.slug).isEqualTo(req.slug)
//
//        val courses = submitGetAllCourses()
//
//        val coursesForUpdatedSubject = courses
//            .filter { it.subject.subjectId == subjectToChange.id }
//
//        coursesForUpdatedSubject.forEach {
//            assertThat(it.subject.subjectId).isEqualTo(subjectToChange.id)
//            assertThat(it.subject.name).isEqualTo(req.name)
//            assertThat(it.subject.slug).isEqualTo(req.slug)
//        }
//    }
//
//    @Test
//    fun createSubject_usesExistingSlug_throwsError() {
//
//        listOf(
//            pythonSubject, swiftSubject
//        )
//
//        val req = SubjectMetadata(
//            name = "Some new subject",
//            slug = pythonSubject.slug,
//            id = 0
//        )
//
//        assertPostSubjecterror(req, ErrorCode.SLUG_EXISTS)
//    }
//
//
//    @Test
//    fun updateSubject_usesExistingSlug_throwsError() {
//
//        listOf(
//            pythonSubject, swiftSubject
//        )
//
//        val subjectToChange = pythonSubject
//
//        val req = SubjectMetadata(
//            name = subjectToChange.name,
//            slug = swiftSubject.slug,
//            id = 0
//        )
//
//        assertPutSubjectError(subjectToChange.id, req, ErrorCode.SLUG_EXISTS)
//    }
//
//    @Test
//    fun createSubject_createsSubject_returnsUpdatedList() {
//
//        val existingSubjects = listOf(pythonSubject, swiftSubject)
//        val newSubjectReq = SubjectMetadata(
//            name = "DSA",
//            slug = "dsa",
//            id = 0
//        )
//
//        val res = submitPostSubject(newSubjectReq)
//        assertThat(res.map { it.slug })
//            .containsExactlyInAnyOrderElementsOf(
//                existingSubjects.map { it.slug } + newSubjectReq.slug
//            )
//    }
//
//    @Test
//    fun deleteSubject_deletesSubject_returnsListExcludingDeleted() {
//        val newSubjectReq = SubjectMetadata(
//            id = 0,
//            name = "DSA",
//            slug = "dsa",
//        )
//
//        val existingSubjects = submitPostSubject(newSubjectReq)
//
//        val subjectToDeleteId = existingSubjects.firstOrNull { it.slug == newSubjectReq.slug }
//            ?.id
//            ?: fail("Created subject not found in response")
//
//        val res = submitDeleteSubject(subjectToDeleteId)
//        assertThat(res.size).isEqualTo(existingSubjects.size - 1)
//        assertThat(res.map { it.id })
//            .doesNotContain(subjectToDeleteId)
//    }
//
//    @Test
//    fun deleteSubject_hasAttachedCourse_throwsError() {
//        val subjectToDeleteId = pythonSubject.id
//        assertDeleteSubjectError(subjectToDeleteId, ErrorCode.SUBJECT_IN_USE)
//    }
//
//
//    private fun submitGetAllCourses(): Array<CourseResponse> =
//        TestRestClient.getOk(
//            "${ApiPaths.CATALOG.BASE}${ApiPaths.CATALOG.COURSES}",
//            user1.id,
//            Array<CourseResponse>::class.java
//        )
//
//    private fun submitGetAllSubjects(): Array<SubjectMetadata> =
//        TestRestClient.getOk(ApiPaths.SUBJECTS.BASE, userId = user1.id, Array<SubjectMetadata>::class.java)
//
//    private fun assertPutSubjectError(
//        subjectId: Long,
//        req: SubjectMetadata,
//        statusCode: ErrorCode
//    ): ValidatableResponse? {
//        return TestRestClient.assertError("PUT", ApiPaths.SUBJECTS.bySubjectAdmin(subjectId), user1.id, req, statusCode)
//    }
//
//    private fun assertPostSubjecterror(req: SubjectMetadata, statusCode: ErrorCode): ValidatableResponse? {
//        return TestRestClient.assertError("POST", ApiPaths.SUBJECTS.ADMIN_BASE, user1.id, req, statusCode)
//    }
//
//    private fun assertDeleteSubjectError(subjectId: Long, statusCode: ErrorCode): ValidatableResponse? {
//        return TestRestClient.assertError(
//            "DELETE",
//            ApiPaths.SUBJECTS.bySubjectAdmin(subjectId),
//            user1.id,
//            null,
//            statusCode
//        )
//    }
//
//    private fun submitPutSubject(subjectId: Long, req: SubjectMetadata): Array<SubjectMetadata> =
//        TestRestClient.putOk(
//            ApiPaths.SUBJECTS.bySubjectAdmin(subjectId),
//            user1.id,
//            req,
//            Array<SubjectMetadata>::class.java
//        )
//
//    private fun submitPostSubject(req: SubjectMetadata): Array<SubjectMetadata> =
//        TestRestClient.postOk(ApiPaths.SUBJECTS.ADMIN_BASE, user1.id, req, Array<SubjectMetadata>::class.java)
//
//    private fun submitDeleteSubject(subjectId: Long): Array<SubjectMetadata> =
//        TestRestClient.deleteOk(
//            ApiPaths.SUBJECTS.bySubjectAdmin(subjectId),
//            user1.id,
//            Array<SubjectMetadata>::class.java
//        )

}