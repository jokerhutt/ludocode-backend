package com.ludocode.ludocodebackend.project.integration
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.jobs.ProjectCleanupJob
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
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
        val (projects, files) = ProjectTestUtil.spawnProjects(
            amount = 6,
            userId = user1.id,
            language = pythonLanguage,
            extension = ".py",
            starterContent = "print('Hello World!')",
            clock = clock,
            storage = storage,
            bucketName = bucketName,
            startDaysAgo = 2
        )
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val newProjectRequest = newProjectRequest("Seventh Project")

        assertErrorOnPost(newProjectRequest, userId = user1.id, ErrorCode.PROJECT_LIMIT_EXCEEDED)

    }

    @Test
    fun createTwoProjects_firstUnderPlanLimit_secondOverLimit_firstSucceeds_secondThrows () {
        val (projects, files) = ProjectTestUtil.spawnProjects(
            amount = 5,
            userId = user1.id,
            language = pythonLanguage,
            extension = ".py",
            starterContent = "print('Hello World!')",
            clock = clock,
            storage = storage,
            bucketName = bucketName,
            startDaysAgo = 2
        )
        projectFileRepository.saveAll(files)
        userProjectRepository.saveAll(projects)

        val newProjectRequest = newProjectRequest("Second Project")

        submitPostCreateProject(newProjectRequest, user1.id)
        val res = submitGetUserProjects(user1.id)
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(6)
        val stillMarked = res.projects.filter { it.deleteAt != null }
        assertThat(stillMarked).isEmpty()

        val secondProjectRequest = newProjectRequest("Seventh Project")

        assertErrorOnPost(secondProjectRequest, userId = user1.id, ErrorCode.PROJECT_LIMIT_EXCEEDED)

    }

    @Test
    fun downGraded_marksDeletedAt() {
        val (projects, files) = ProjectTestUtil.spawnProjects(
            amount = 7,
            userId = user1.id,
            language = pythonLanguage,
            extension = ".py",
            starterContent = "print('Hello World!')",
            clock = clock,
            storage = storage,
            bucketName = bucketName,
            startDaysAgo = 2
        )
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

        val (projects, files) = ProjectTestUtil.spawnProjects(
            amount = 7,
            userId = user1.id,
            language = pythonLanguage,
            extension = ".py",
            starterContent = "print('Hello World!')",
            clock = clock,
            storage = storage,
            bucketName = bucketName,
            startDaysAgo = 2
        )
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

        val (projects, files) = ProjectTestUtil.spawnProjects(
            amount = 7,
            userId = user1.id,
            language = pythonLanguage,
            extension = ".py",
            starterContent = "print('Hello World!')",
            clock = clock,
            storage = storage,
            bucketName = bucketName,
            startDaysAgo = 2
        )
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

    private fun newProjectRequest(name: String): CreateProjectRequest =
        CreateProjectRequest(
            projectName = name,
            projectType = ProjectType.CODE,
            files = listOf(
                ProjectFileSnapshot(
                    id = null,
                    path = "script.py",
                    language = pythonLanguage,
                    content = "print('Hello World!')"
                )
            ),
            entryFilePath = "script.py",
            requestHash = UUID.randomUUID()
        )




}