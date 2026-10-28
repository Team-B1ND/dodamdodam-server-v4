package com.b1nd.dodamdodam.nightstudy.application.room

import com.b1nd.dodamdodam.grpc.user.GetUsersResponse
import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyRoomMemberCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.UUID

class ProjectRoomUseCaseTest {
    private val projectRoomService = mock(ProjectRoomService::class.java)
    private val nightStudyService = mock(NightStudyService::class.java)
    private val attendanceService = mock(NightStudyAttendanceService::class.java)
    private val userQueryClient = mock(UserQueryClient::class.java)
    private val useCase = ProjectRoomUseCase(
        projectRoomService = projectRoomService,
        nightStudyService = nightStudyService,
        nightStudyAttendanceService = attendanceService,
        userQueryClient = userQueryClient,
    )

    @Test
    fun `3학년은 반에 관계없이 하나의 심자실로 분류한다`() {
        val date = LocalDate.of(2026, 8, 1)
        val firstUserId = UUID.randomUUID()
        val secondUserId = UUID.randomUUID()
        val assignments = listOf(
            personalAssignment(firstUserId, period = 1),
            personalAssignment(secondUserId, period = 1),
        )

        `when`(projectRoomService.getAll()).thenReturn(emptyList())
        `when`(nightStudyService.getAllowedRoomMembers(date, 1)).thenReturn(assignments)
        `when`(attendanceService.getAttendedUserIds(setOf(firstUserId, secondUserId), date, 1))
            .thenReturn(setOf(firstUserId))
        stubUsers(
            student(firstUserId, "첫 번째 학생", grade = 3, room = 1, number = 1),
            student(secondUserId, "두 번째 학생", grade = 3, room = 4, number = 2),
        )

        val response = useCase.getAllWithStatus(date, 1)
        val thirdGradeRoom = response.data!!.single { it.roomId == "GRADE_3" }

        assertEquals(2, thirdGradeRoom.memberCount)
        assertEquals(1, thirdGradeRoom.unchecked)
    }

    @Test
    fun `프로젝트 심자와 개인 심자가 중복되면 프로젝트실에만 배정한다`() {
        val date = LocalDate.of(2026, 8, 1)
        val userId = UUID.randomUUID()
        val projectRoom = mock(ProjectRoomEntity::class.java)
        val assignments = listOf(
            personalAssignment(userId, period = 2),
            projectAssignment(userId, period = 2, projectRoomId = 12L),
        )

        `when`(projectRoom.id).thenReturn(12L)
        `when`(projectRoom.name).thenReturn("LAB13")
        `when`(projectRoomService.getAll()).thenReturn(listOf(projectRoom))
        `when`(nightStudyService.getAllowedRoomMembers(date, 2)).thenReturn(assignments)
        `when`(attendanceService.getAttendedUserIds(setOf(userId), date, 2)).thenReturn(emptySet())
        stubUsers(student(userId, "프로젝트 학생", grade = 1, room = 1, number = 1))

        val response = useCase.getAllWithStatus(date, 2)
        val project = response.data!!.single { it.roomId == "PROJECT_12" }
        val classRoom = response.data!!.single { it.roomId == "CLASS_1_1" }

        assertEquals(1, project.memberCount)
        assertEquals(1, project.unchecked)
        assertEquals(0, classRoom.memberCount)
    }

    @Test
    fun `심1 개인 심자와 심2 프로젝트 심자가 있으면 심1에는 개인 심자실로 배정한다`() {
        val date = LocalDate.of(2026, 8, 13)
        val userId = UUID.randomUUID()
        val projectRoom = mock(ProjectRoomEntity::class.java)
        val assignments = listOf(
            projectAssignment(userId, period = 2, projectRoomId = 13L),
            personalAssignment(userId, period = 1),
        )

        `when`(projectRoom.id).thenReturn(13L)
        `when`(projectRoom.name).thenReturn("LAB 13")
        `when`(projectRoomService.getAll()).thenReturn(listOf(projectRoom))
        `when`(nightStudyService.getAllowedRoomMembers(date, 1)).thenReturn(assignments)
        `when`(attendanceService.getAttendedUserIds(setOf(userId), date, 1)).thenReturn(emptySet())
        stubUsers(student(userId, "프로젝트 학생", grade = 1, room = 2, number = 1))

        val response = useCase.getAllWithStatus(date, 1)
        val project = response.data!!.single { it.roomId == "PROJECT_13" }
        val classRoom = response.data!!.single { it.roomId == "CLASS_1_2" }

        assertEquals(0, project.memberCount)
        assertEquals(1, classRoom.memberCount)
    }

    @Test
    fun `심1과 심2 프로젝트실이 다르면 요청 교시 프로젝트실로 배정한다`() {
        val date = LocalDate.of(2026, 8, 13)
        val userId = UUID.randomUUID()
        val firstPeriodRoom = mock(ProjectRoomEntity::class.java)
        val secondPeriodRoom = mock(ProjectRoomEntity::class.java)
        val assignments = listOf(
            projectAssignment(userId, period = 2, projectRoomId = 13L),
            projectAssignment(userId, period = 1, projectRoomId = 1L),
        )

        `when`(firstPeriodRoom.id).thenReturn(1L)
        `when`(firstPeriodRoom.name).thenReturn("LAB 1")
        `when`(secondPeriodRoom.id).thenReturn(13L)
        `when`(secondPeriodRoom.name).thenReturn("LAB 13")
        `when`(projectRoomService.getAll()).thenReturn(listOf(firstPeriodRoom, secondPeriodRoom))
        `when`(nightStudyService.getAllowedRoomMembers(date, 1)).thenReturn(assignments)
        `when`(attendanceService.getAttendedUserIds(setOf(userId), date, 1)).thenReturn(emptySet())
        stubUsers(student(userId, "프로젝트 학생", grade = 1, room = 1, number = 1))

        val response = useCase.getAllWithStatus(date, 1)
        val firstPeriodProject = response.data!!.single { it.roomId == "PROJECT_1" }
        val secondPeriodProject = response.data!!.single { it.roomId == "PROJECT_13" }

        assertEquals(1, firstPeriodProject.memberCount)
        assertEquals(0, secondPeriodProject.memberCount)
    }

    @Test
    fun `심2 프로젝트 심자 인원은 심1 프로젝트실 인원에 포함하지 않는다`() {
        val date = LocalDate.of(2026, 8, 13)
        val userId = UUID.randomUUID()
        val projectRoom = mock(ProjectRoomEntity::class.java)
        val assignments = listOf(projectAssignment(userId, period = 2, projectRoomId = 13L))

        `when`(projectRoom.id).thenReturn(13L)
        `when`(projectRoom.name).thenReturn("LAB 13")
        `when`(projectRoomService.getAll()).thenReturn(listOf(projectRoom))
        `when`(nightStudyService.getAllowedRoomMembers(date, 1)).thenReturn(assignments)
        `when`(nightStudyService.getAllowedRoomMembers(date, 2)).thenReturn(assignments)
        `when`(attendanceService.getAttendedUserIds(setOf(userId), date, 2)).thenReturn(emptySet())
        stubUsers(student(userId, "프로젝트 학생", grade = 1, room = 1, number = 1))

        val firstPeriod = useCase.getByRoomId("PROJECT_13", date, 1)
        val secondPeriod = useCase.getByRoomId("PROJECT_13", date, 2)

        assertEquals(0, firstPeriod.data!!.roomMember.size)
        assertEquals(1, secondPeriod.data!!.roomMember.size)
    }

    @Test
    fun `반 심자실은 심1에 심1과 심2 인원을 함께 보여주고 심2에는 심2 인원만 보여준다`() {
        val date = LocalDate.of(2026, 8, 13)
        val firstPeriodUserId = UUID.randomUUID()
        val secondPeriodUserId = UUID.randomUUID()
        val firstPeriodAssignment = personalAssignment(firstPeriodUserId, period = 1)
        val secondPeriodAssignment = personalAssignment(secondPeriodUserId, period = 2)

        `when`(projectRoomService.getAll()).thenReturn(emptyList())
        `when`(nightStudyService.getAllowedRoomMembers(date, 1))
            .thenReturn(listOf(firstPeriodAssignment, secondPeriodAssignment))
        `when`(nightStudyService.getAllowedRoomMembers(date, 2)).thenReturn(listOf(secondPeriodAssignment))
        `when`(attendanceService.getAttendedUserIds(setOf(firstPeriodUserId, secondPeriodUserId), date, 1))
            .thenReturn(emptySet())
        `when`(attendanceService.getAttendedUserIds(setOf(secondPeriodUserId), date, 2)).thenReturn(emptySet())
        stubUsers(
            student(firstPeriodUserId, "심1 학생", grade = 1, room = 1, number = 1),
            student(secondPeriodUserId, "심2 학생", grade = 1, room = 1, number = 2),
        )

        val firstPeriod = useCase.getByRoomId("CLASS_1_1", date, 1)
        val secondPeriod = useCase.getByRoomId("CLASS_1_1", date, 2)

        assertEquals(listOf("심1 학생", "심2 학생"), firstPeriod.data!!.roomMember.map { it.name })
        assertEquals(listOf("심2 학생"), secondPeriod.data!!.roomMember.map { it.name })
    }

    private fun personalAssignment(userId: UUID, period: Int) = NightStudyRoomMemberCommand(
        userId = userId,
        type = NightStudyType.PERSONAL,
        period = period,
        projectRoomId = null,
    )

    private fun projectAssignment(userId: UUID, period: Int, projectRoomId: Long) = NightStudyRoomMemberCommand(
        userId = userId,
        type = NightStudyType.PROJECT,
        period = period,
        projectRoomId = projectRoomId,
    )

    private fun student(
        userId: UUID,
        name: String,
        grade: Int,
        room: Int,
        number: Int,
    ): UserResponse = UserResponse.newBuilder()
        .setPublicId(userId.toString())
        .setName(name)
        .setStudent(
            StudentInfo.newBuilder()
                .setGrade(grade)
                .setRoom(room)
                .setNumber(number)
        )
        .build()

    private fun stubUsers(vararg users: UserResponse) {
        val response = GetUsersResponse.newBuilder().addAllUsers(users.toList()).build()
        runBlocking {
            `when`(userQueryClient.getUsers(anyList())).thenReturn(response)
        }
    }
}
