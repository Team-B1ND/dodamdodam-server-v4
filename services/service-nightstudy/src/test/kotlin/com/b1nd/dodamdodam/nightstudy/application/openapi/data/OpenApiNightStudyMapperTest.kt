package com.b1nd.dodamdodam.nightstudy.application.openapi.data

import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participations
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.ProjectRoom
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.ABSENT
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.ATTENDANCE
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.NOT_APPLIED
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class OpenApiNightStudyMapperTest {

    @Test
    fun `1차만 출석하면 period1은 ATTENDANCE period2는 신청 여부를 따른다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(leaderId to participation(ATTENDANCE, ABSENT))),
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals(ATTENDANCE, leader.attended.period1)
        assertEquals(ABSENT, leader.attended.period2)
    }

    @Test
    fun `1차 심자 참여자도 2차 출석이면 period2가 ATTENDANCE로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            period = 1,
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(leaderId to participation(ABSENT, ATTENDANCE))),
        )

        val response = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single()

        assertEquals(1, response.period)
        assertEquals(ABSENT, response.leader.attended.period1)
        assertEquals(ATTENDANCE, response.leader.attended.period2)
    }

    @Test
    fun `신청했지만 출석 기록이 없으면 ABSENT로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(leaderId to participation(ABSENT, ABSENT))),
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals(ABSENT, leader.attended.period1)
        assertEquals(ABSENT, leader.attended.period2)
    }

    @Test
    fun `참여 정보가 없는 학생은 NOT_APPLIED와 빈 방으로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations.EMPTY,
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals(NOT_APPLIED, leader.attended.period1)
        assertEquals(NOT_APPLIED, leader.attended.period2)
        assertNull(leader.projectRoom.period1)
        assertNull(leader.projectRoom.period2)
    }

    @Test
    fun `ABSENT와 NOT_APPLIED를 구분한다`() {
        val absentId = UUID.randomUUID()
        val notAppliedId = UUID.randomUUID()
        val command = command(
            leaderId = absentId,
            memberIds = listOf(absentId, notAppliedId),
            participations = Participations(mapOf(absentId to participation(ABSENT, ABSENT))),
        )

        val response = listOf(command)
            .toOpenApiNightStudyResponse(userMap(student(absentId), student(notAppliedId)))
            .single()

        assertEquals(ABSENT, response.members[0].attended.period1)
        assertEquals(NOT_APPLIED, response.members[1].attended.period1)
    }

    @Test
    fun `배정된 프로젝트실을 차수별로 내려준다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(
                leaderId to participation(ATTENDANCE, ATTENDANCE, room1 = "정보관 301", room2 = "정보관 302"),
            )),
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals("정보관 301", leader.projectRoom.period1)
        assertEquals("정보관 302", leader.projectRoom.period2)
    }

    @Test
    fun `프로젝트 심자를 한 차수만 하면 나머지 차수의 방은 null이다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(
                leaderId to participation(ATTENDANCE, ATTENDANCE, room1 = "정보관 301", room2 = null),
            )),
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals("정보관 301", leader.projectRoom.period1)
        assertNull(leader.projectRoom.period2)
    }

    @Test
    fun `personal 심자만 있으면 두 차수 모두 방이 null이다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            type = NightStudyType.PERSONAL,
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(leaderId to participation(ATTENDANCE, ATTENDANCE))),
        )

        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single().leader

        assertEquals(ATTENDANCE, leader.attended.period1)
        assertNull(leader.projectRoom.period1)
        assertNull(leader.projectRoom.period2)
    }

    @Test
    fun `방 배정은 학생마다 개별로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId, memberId),
            participations = Participations(mapOf(
                leaderId to participation(ATTENDANCE, ABSENT, room1 = "정보관 301", room2 = null),
                memberId to participation(ABSENT, ATTENDANCE, room1 = null, room2 = "정보관 302"),
            )),
        )

        val response = listOf(command)
            .toOpenApiNightStudyResponse(userMap(student(leaderId), student(memberId)))
            .single()

        assertEquals("정보관 301" to null, response.members[0].projectRoom.let { it.period1 to it.period2 })
        assertEquals(null to "정보관 302", response.members[1].projectRoom.let { it.period1 to it.period2 })
    }

    @Test
    fun `출석 상태는 학생마다 개별로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val absentId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId, memberId, absentId),
            participations = Participations(mapOf(
                leaderId to participation(ATTENDANCE, ABSENT),
                memberId to participation(ABSENT, ATTENDANCE),
                absentId to participation(ABSENT, ABSENT),
            )),
        )

        val response = listOf(command)
            .toOpenApiNightStudyResponse(userMap(student(leaderId), student(memberId), student(absentId)))
            .single()

        assertEquals(3, response.members.size)
        assertEquals(ATTENDANCE to ABSENT, response.members[0].attended.let { it.period1 to it.period2 })
        assertEquals(ABSENT to ATTENDANCE, response.members[1].attended.let { it.period1 to it.period2 })
        assertEquals(ABSENT to ABSENT, response.members[2].attended.let { it.period1 to it.period2 })
    }

    @Test
    fun `리더 정보를 찾지 못하면 해당 심자를 응답에서 제외한다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId),
            participations = Participations(mapOf(leaderId to participation(ATTENDANCE, ABSENT))),
        )

        assertTrue(listOf(command).toOpenApiNightStudyResponse(emptyMap()).isEmpty())
    }

    @Test
    fun `리더가 없는 심자는 응답에서 제외한다`() {
        val command = command(
            leaderId = null,
            memberIds = emptyList(),
            participations = Participations.EMPTY,
        )

        assertTrue(listOf(command).toOpenApiNightStudyResponse(emptyMap()).isEmpty())
    }

    @Test
    fun `attended와 projectRoom은 차수 필드를 가진 객체로 직렬화된다`() {
        val leaderId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = emptyList(),
            participations = Participations(mapOf(
                leaderId to participation(ATTENDANCE, NOT_APPLIED, room1 = "정보관 301", room2 = null),
            )),
        )
        val response = listOf(command).toOpenApiNightStudyResponse(userMap(student(leaderId))).single()

        val json = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule()).writeValueAsString(response)

        assertTrue(json.contains("\"attended\":{\"period1\":\"ATTENDANCE\",\"period2\":\"NOT_APPLIED\"}"), json)
        assertTrue(json.contains("\"projectRoom\":{\"period1\":\"정보관 301\",\"period2\":null}"), json)
    }

    private fun participation(
        period1: com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus,
        period2: com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus,
        room1: String? = null,
        room2: String? = null,
    ) = Participation(
        attendance = Attendance(period1 = period1, period2 = period2),
        projectRoom = ProjectRoom(period1 = room1, period2 = room2),
    )

    private fun command(
        period: Int = 1,
        type: NightStudyType = NightStudyType.PROJECT,
        leaderId: UUID?,
        memberIds: List<UUID>,
        participations: Participations,
    ) = NightStudyWithMembersCommand(
        nightStudy = NightStudyEntity(
            name = "심자",
            description = "설명",
            period = period,
            startAt = LocalDate.of(2026, 9, 1),
            endAt = LocalDate.of(2026, 9, 30),
            needPhone = false,
            status = NightStudyStatusType.ALLOWED,
            type = type,
        ),
        leaderId = leaderId,
        memberIds = memberIds,
        participations = participations,
    )

    private fun student(userId: UUID): UserResponse = UserResponse.newBuilder()
        .setPublicId(userId.toString())
        .setName("학생")
        .setGender("MALE")
        .setStudent(StudentInfo.newBuilder().setGrade(1).setRoom(2).setNumber(3))
        .build()

    private fun userMap(vararg users: UserResponse): Map<String?, UserResponse> =
        users.associateBy { it.publicId }
}
