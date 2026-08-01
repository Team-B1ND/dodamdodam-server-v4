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
            personalAssignment(firstUserId),
            personalAssignment(secondUserId),
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
            personalAssignment(userId),
            NightStudyRoomMemberCommand(userId, NightStudyType.PROJECT, projectRoomId = 12L),
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

    private fun personalAssignment(userId: UUID) = NightStudyRoomMemberCommand(
        userId = userId,
        type = NightStudyType.PERSONAL,
        projectRoomId = null,
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
