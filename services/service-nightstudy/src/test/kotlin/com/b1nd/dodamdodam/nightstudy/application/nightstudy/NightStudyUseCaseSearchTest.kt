package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingResponse
import com.b1nd.dodamdodam.grpc.outsleeping.OutSleeping
import com.b1nd.dodamdodam.grpc.user.GetUsersResponse
import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.outSleeping.client.OutSleepingClient
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.UUID

class NightStudyUseCaseSearchTest {
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
    fun `개인 심자 신청자가 외박 중이면 목록에서 제외한다`() {
        val userId = UUID.randomUUID()
        val personal = nightStudy(id = 1L, type = NightStudyType.PERSONAL)

        stubNightStudies(NightStudyType.PERSONAL, personal to (userId to listOf(userId)))
        stubUsers(student(userId, number = 1))
        stubOutSleeping(outSleeping(userId))

        val result = useCase.searchAllByType(NightStudyType.PERSONAL, null, null, PageRequest.of(0, 20)).data!!

        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `프로젝트 멤버가 외박 중이면 해당 멤버만 목록에서 제외한다`() {
        val leaderId = UUID.randomUUID()
        val outSleepingMemberId = UUID.randomUUID()
        val project = nightStudy(id = 1L, type = NightStudyType.PROJECT)

        stubNightStudies(NightStudyType.PROJECT, project to (leaderId to listOf(leaderId, outSleepingMemberId)))
        stubUsers(student(leaderId, number = 1), student(outSleepingMemberId, number = 2))
        stubOutSleeping(outSleeping(outSleepingMemberId))

        val result = useCase.searchAllByType(NightStudyType.PROJECT, null, null, PageRequest.of(0, 20)).data!!

        assertEquals(listOf(leaderId), result.content.single().members.map { it.publicId })
    }

    @Test
    fun `프로젝트 리더가 외박 중이어도 신청과 리더는 보이고 멤버에서만 제외한다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = nightStudy(id = 1L, type = NightStudyType.PROJECT)

        stubNightStudies(NightStudyType.PROJECT, project to (leaderId to listOf(leaderId, memberId)))
        stubUsers(student(leaderId, number = 1), student(memberId, number = 2))
        stubOutSleeping(outSleeping(leaderId))

        val result = useCase.searchAllByType(NightStudyType.PROJECT, null, null, PageRequest.of(0, 20)).data!!

        val application = result.content.single()
        assertEquals(leaderId, application.leader.publicId)
        assertEquals(listOf(memberId), application.members.map { it.publicId })
    }

    @Test
    fun `프로젝트 멤버가 모두 외박 중이면 목록에서 제외한다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = nightStudy(id = 1L, type = NightStudyType.PROJECT)

        stubNightStudies(NightStudyType.PROJECT, project to (leaderId to listOf(leaderId, memberId)))
        stubUsers(student(leaderId, number = 1), student(memberId, number = 2))
        stubOutSleeping(outSleeping(leaderId), outSleeping(memberId))

        val result = useCase.searchAllByType(NightStudyType.PROJECT, null, null, PageRequest.of(0, 20)).data!!

        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `외박 조회는 리더가 아닌 멤버 전원으로 요청한다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = nightStudy(id = 1L, type = NightStudyType.PROJECT)

        stubNightStudies(NightStudyType.PROJECT, project to (leaderId to listOf(leaderId, memberId)))
        stubUsers(student(leaderId, number = 1), student(memberId, number = 2))
        stubOutSleeping()

        useCase.searchAllByType(NightStudyType.PROJECT, null, null, PageRequest.of(0, 20))

        runBlocking {
            verify(outSleepingClient).getOutSleeping(listOf(leaderId, memberId))
        }
    }

    private fun stubNightStudies(
        type: NightStudyType,
        vararg nightStudies: Pair<NightStudyEntity, Pair<UUID, List<UUID>>>,
    ) {
        val entities = nightStudies.map { it.first }
        val leaders = nightStudies.associate { (nightStudy, members) -> nightStudy.id!! to members.first }
        val memberIds = nightStudies.associate { (nightStudy, members) -> nightStudy.id!! to members.second }
        `when`(nightStudyService.searchByType(type, null, null)).thenReturn(entities)
        `when`(nightStudyService.getLeadersByNightStudies(entities)).thenReturn(leaders)
        `when`(nightStudyService.getMembersByNightStudies(entities)).thenReturn(memberIds)
    }

    private fun nightStudy(
        id: Long,
        type: NightStudyType,
    ): NightStudyEntity = mock(NightStudyEntity::class.java).also { nightStudy ->
        `when`(nightStudy.id).thenReturn(id)
        `when`(nightStudy.publicId).thenReturn(UUID.randomUUID())
        `when`(nightStudy.type).thenReturn(type)
        `when`(nightStudy.period).thenReturn(1)
        `when`(nightStudy.description).thenReturn("심자")
        `when`(nightStudy.startAt).thenReturn(LocalDate.now())
        `when`(nightStudy.endAt).thenReturn(LocalDate.now())
        `when`(nightStudy.status).thenReturn(NightStudyStatusType.PENDING)
    }

    private fun student(userId: UUID, number: Int): UserResponse = UserResponse.newBuilder()
        .setPublicId(userId.toString())
        .setName("학생")
        .setStudent(
            StudentInfo.newBuilder()
                .setGrade(1)
                .setRoom(1)
                .setNumber(number)
        )
        .build()

    private fun outSleeping(userId: UUID): OutSleeping = OutSleeping.newBuilder()
        .setUserId(userId.toString())
        .setStartAt(LocalDate.now().minusDays(1).toString())
        .setEndAt(LocalDate.now().plusDays(1).toString())
        .setStatus("ALLOWED")
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
