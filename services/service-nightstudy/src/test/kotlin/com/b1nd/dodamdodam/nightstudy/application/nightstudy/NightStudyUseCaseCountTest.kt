package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingResponse
import com.b1nd.dodamdodam.grpc.outsleeping.OutSleeping
import com.b1nd.dodamdodam.grpc.user.GetUsersResponse
import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.outSleeping.client.OutSleepingClient
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.UUID

class NightStudyUseCaseCountTest {
    private val nightStudyService = mock(NightStudyService::class.java)
    private val nightStudyAttendanceService = mock(NightStudyAttendanceService::class.java)
    private val projectRoomService = mock(ProjectRoomService::class.java)
    private val userQueryClient = mock(UserQueryClient::class.java)
    private val outSleepingClient = mock(OutSleepingClient::class.java)
    private val useCase = NightStudyUseCase(
        nightStudyService = nightStudyService,
        nightStudyAttendanceService = nightStudyAttendanceService,
        projectRoomService = projectRoomService,
        userQueryClient = userQueryClient,
        outSleepingClient = outSleepingClient,
    )

    @Test
    fun `개인과 프로젝트 심자가 겹쳐도 사용자와 차수별로 한 번만 집계한다`() {
        val userId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL, period = 2)
        val projectRoom = mock(ProjectRoomEntity::class.java)
        val project = nightStudy(id = 2L, type = NightStudyType.PROJECT, period = 1, room = projectRoom)

        `when`(projectRoom.floor).thenReturn(3)
        `when`(nightStudyService.getAllAllowed()).thenReturn(listOf(personal, project))
        `when`(nightStudyService.getMembersByNightStudies(listOf(personal, project))).thenReturn(
            mapOf(
                1L to listOf(userId),
                2L to listOf(userId),
            )
        )
        stubUsers(student(userId, grade = 1, room = 1, number = 1))
        stubOutSleeping()

        val result = useCase.countTotalMembers().data!!

        assertEquals(1, result.total.period1.project)
        assertEquals(0, result.total.period1.personal)
        assertEquals(0, result.total.period2.project)
        assertEquals(1, result.total.period2.personal)
        assertEquals(1, result.floors.single { it.floor == 3 }.count.period1.project)
        assertEquals(1, result.floors.single { it.floor == 2 }.count.period2.personal)
        assertEquals(1, result.grades.single { it.grade == 1 }.count.period1.project)
        assertEquals(1, result.grades.single { it.grade == 1 }.count.period2.personal)
    }

    @Test
    fun `외박 중인 학생은 인원수에서 제외한다`() {
        val outSleepingUserId = UUID.randomUUID()
        val attendingUserId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL, period = 2)

        `when`(nightStudyService.getAllAllowed()).thenReturn(listOf(personal))
        `when`(nightStudyService.getMembersByNightStudies(listOf(personal))).thenReturn(
            mapOf(1L to listOf(outSleepingUserId, attendingUserId))
        )
        stubUsers(
            student(outSleepingUserId, grade = 1, room = 1, number = 1),
            student(attendingUserId, grade = 1, room = 1, number = 2),
        )
        stubOutSleeping(outSleeping(outSleepingUserId))

        val result = useCase.countTotalMembers().data!!

        assertEquals(1, result.total.period1.personal)
        assertEquals(1, result.total.period2.personal)
        assertEquals(1, result.grades.single { it.grade == 1 }.count.period1.personal)
    }

    @Test
    fun `외박 기간이 오늘이 아니면 인원수에 포함한다`() {
        val userId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL, period = 1)

        `when`(nightStudyService.getAllAllowed()).thenReturn(listOf(personal))
        `when`(nightStudyService.getMembersByNightStudies(listOf(personal))).thenReturn(
            mapOf(1L to listOf(userId))
        )
        stubUsers(student(userId, grade = 1, room = 1, number = 1))
        stubOutSleeping(
            outSleeping(userId, startAt = LocalDate.now().minusDays(3), endAt = LocalDate.now().minusDays(1))
        )

        val result = useCase.countTotalMembers().data!!

        assertEquals(1, result.total.period1.personal)
    }

    @Test
    fun `승인되지 않은 외박은 인원수에서 제외하지 않는다`() {
        val userId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL, period = 1)

        `when`(nightStudyService.getAllAllowed()).thenReturn(listOf(personal))
        `when`(nightStudyService.getMembersByNightStudies(listOf(personal))).thenReturn(
            mapOf(1L to listOf(userId))
        )
        stubUsers(student(userId, grade = 1, room = 1, number = 1))
        stubOutSleeping(outSleeping(userId, status = "PENDING"))

        val result = useCase.countTotalMembers().data!!

        assertEquals(1, result.total.period1.personal)
    }

    @Test
    fun `성별별로 인원수를 집계한다`() {
        val maleUserId = UUID.randomUUID()
        val femaleUserId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL, period = 1)

        `when`(nightStudyService.getAllAllowed()).thenReturn(listOf(personal))
        `when`(nightStudyService.getMembersByNightStudies(listOf(personal))).thenReturn(
            mapOf(1L to listOf(maleUserId, femaleUserId))
        )
        stubUsers(
            student(maleUserId, grade = 1, room = 1, number = 1, gender = "MALE"),
            student(femaleUserId, grade = 2, room = 1, number = 2, gender = "FEMALE"),
        )
        stubOutSleeping()

        val result = useCase.countTotalMembers().data!!

        assertEquals(1, result.genders.single { it.gender == "MALE" }.count.period1.personal)
        assertEquals(1, result.genders.single { it.gender == "FEMALE" }.count.period1.personal)
        assertEquals(0, result.genders.single { it.gender == "MALE" }.count.period2.personal)
        assertEquals(2, result.total.period1.personal)
    }

    private fun nightStudy(
        id: Long,
        type: NightStudyType,
        period: Int,
        room: ProjectRoomEntity? = null,
    ): NightStudyEntity = mock(NightStudyEntity::class.java).also { nightStudy ->
        `when`(nightStudy.id).thenReturn(id)
        `when`(nightStudy.type).thenReturn(type)
        `when`(nightStudy.period).thenReturn(period)
        `when`(nightStudy.room).thenReturn(room)
    }

    private fun student(
        userId: UUID,
        grade: Int,
        room: Int,
        number: Int,
        gender: String = "MALE",
    ): UserResponse = UserResponse.newBuilder()
        .setPublicId(userId.toString())
        .setName("학생")
        .setGender(gender)
        .setStudent(
            StudentInfo.newBuilder()
                .setGrade(grade)
                .setRoom(room)
                .setNumber(number)
        )
        .build()

    private fun outSleeping(
        userId: UUID,
        startAt: LocalDate = LocalDate.now(),
        endAt: LocalDate = LocalDate.now(),
        status: String = "ALLOWED",
    ): OutSleeping = OutSleeping.newBuilder()
        .setUserId(userId.toString())
        .setStartAt(startAt.toString())
        .setEndAt(endAt.toString())
        .setStatus(status)
        .build()

    private fun stubOutSleeping(vararg outSleepings: OutSleeping) {
        val response = GetOutSleepingResponse.newBuilder().addAllOutSleepings(outSleepings.toList()).build()
        runBlocking {
            `when`(outSleepingClient.getOutSleeping(anyList())).thenReturn(response)
        }
    }

    private fun stubUsers(vararg users: UserResponse) {
        val response = GetUsersResponse.newBuilder().addAllUsers(users.toList()).build()
        runBlocking {
            `when`(userQueryClient.getUsers(anyList())).thenReturn(response)
        }
    }
}
