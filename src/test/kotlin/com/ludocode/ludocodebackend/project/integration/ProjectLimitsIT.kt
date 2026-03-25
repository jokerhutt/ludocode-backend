package com.ludocode.ludocodebackend.project.integration
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.jobs.ProjectCleanupJob
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardResponse
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.projects.app.service.ProjectPlanEnforcer
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.util.ProjectTestUtil
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test


class ProjectLimitsIT : AbstractIntegrationTest() {


    @Autowired
    private lateinit var projectCleanupJob: ProjectCleanupJob

    @Autowired
    private lateinit var projectPlanEnforcer: ProjectPlanEnforcer

    private val bucketName = "lumo-file-content"

    @BeforeEach
    fun ensureBucketAndClear() {
        try {
            storage.create(BucketInfo.of(bucketName))
        } catch (_: Exception) {
        }

        storage.list(bucketName).iterateAll().forEach { blob ->
            storage.delete(blob.blobId)
        }
    }

    @Test
    fun overPlanLimit_attemptCreateProject_throwsError () {
        val (projects, files) = ProjectTestUtil.spawnProjects(6, user1.id, pythonLanguage, clock, storage, bucketName, 2)
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val newProjectRequest = CreateProjectRequest(
            projectName = "Seventh Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )

        assertErrorOnPost(newProjectRequest, userId = user1.id, ErrorCode.PROJECT_LIMIT_EXCEEDED)

    }

    @Test
    fun createTwoProjects_firstUnderPlanLimit_secondOverLimit_firstSucceeds_secondThrows () {
        val (projects, files) = ProjectTestUtil.spawnProjects(5, user1.id, pythonLanguage, clock, storage, bucketName, 2)
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val newProjectRequest = CreateProjectRequest(
            projectName = "Second Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )

        submitPostCreateProject(newProjectRequest, user1.id)
        val res = submitGetUserProjects(user1.id)
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(6)
        val stillMarked = res.projects.filter { it.deleteAt != null }
        assertThat(stillMarked).isEmpty()

        val secondProjectRequest = CreateProjectRequest(
            projectName = "Seventh Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )

        assertErrorOnPost(secondProjectRequest, userId = user1.id, ErrorCode.PROJECT_LIMIT_EXCEEDED)

    }

    @Test
    fun downGraded_marksDeletedAt() {
        val (projects, files) = ProjectTestUtil.spawnProjects(7, user1.id, pythonLanguage, clock, storage, bucketName, 2)
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val freeLimit = PlanDefinitions.configFor(Plan.FREE).limits.maxProjects
        projectPlanEnforcer.enforcePlanLimit(user1.id, freeLimit)

        val res = submitGetUserProjects(userId = user1.id)

        assertThat(res.projects.size).isEqualTo(7)

        val markedForDeletion = res.projects.filter { it.deleteAt != null }

        assertThat(markedForDeletion).hasSize(1)

        val oldest = res.projects
            .minByOrNull { p: ProjectCardResponse ->
                p.updatedAt ?: OffsetDateTime.MAX
            }!!

        assertThat(oldest.deleteAt).isNotNull()

    }

    @Test
    fun upgraded_unmarksDeletedAt() {

        val now = OffsetDateTime.now(clock)

        val (projects, files) = ProjectTestUtil.spawnProjects(7, user1.id, pythonLanguage, clock, storage, bucketName, 2)
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val freeLimit = PlanDefinitions.configFor(Plan.FREE).limits.maxProjects
        projectPlanEnforcer.enforcePlanLimit(user1.id, freeLimit)

        val res = submitGetUserProjects(userId = user1.id)

        assertThat(res.projects.size).isEqualTo(7)

        val markedForDeletion = res.projects.filter { it.deleteAt != null }

        assertThat(markedForDeletion).hasSize(1)

        val oldest = res.projects
            .minByOrNull { p: ProjectCardResponse ->
                p.updatedAt ?: OffsetDateTime.MAX
            }!!

        assertThat(oldest.deleteAt).isNotNull()

        val supporterPlanLimit = PlanDefinitions.configFor(Plan.SUPPORTER).limits.maxProjects
        projectPlanEnforcer.enforcePlanLimit(user1.id, supporterPlanLimit)

        val newRes = submitGetUserProjects(userId = user1.id)

        assertThat(newRes.projects).hasSize(7)

        val stillMarked = newRes.projects.filter { it.deleteAt != null }

        assertThat(stillMarked).isEmpty()

    }

    @Test
    fun upgraded_thenDowngraded_thenUpgraded_unmarksDeletedAt() {

        val now = OffsetDateTime.now(clock)

        val (projects, files) = ProjectTestUtil.spawnProjects(7, user1.id, pythonLanguage, clock, storage, bucketName, 2)
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val freeLimit = PlanDefinitions.configFor(Plan.FREE).limits.maxProjects
        projectPlanEnforcer.enforcePlanLimit(user1.id, freeLimit)

        val res = submitGetUserProjects(userId = user1.id)

        assertThat(res.projects.size).isEqualTo(7)

        clock.set(clock.instant().plus(21, ChronoUnit.DAYS))
        projectCleanupJob.execute()

        val newRes = submitGetUserProjects(userId = user1.id)

        assertThat(newRes.projects.size).isEqualTo(6)

        val stillMarked = newRes.projects.filter { it.deleteAt != null }

        assertThat(stillMarked).isEmpty()

    }

    private fun submitGetUserProjects(userId: UUID): ProjectCardListResponse =
        TestRestClient.getOk(ApiPaths.PROJECTS.BASE, userId, ProjectCardListResponse::class.java)

    private fun assertErrorOnPost(request: CreateProjectRequest, userId: UUID, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("POST", ApiPaths.PROJECTS.BASE, userId, request, errorCode)

    private fun submitPostCreateProject(request: CreateProjectRequest, userId: UUID) =
        TestRestClient.postNoContent(ApiPaths.PROJECTS.BASE, userId, request)




}