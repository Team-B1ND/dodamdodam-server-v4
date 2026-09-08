package com.b1nd.dodamdodam.nightstudy.application.openapi.data

import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Assignment
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participations
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.PeriodAssignment
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus
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
    fun `프로젝트실이 배정되면 그 실의 이름과 층을 내려준다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(project("정보관 301", 3), project("정보관 302", 2)))

        assertEquals("정보관 301", response.leader.studyRoom.period1?.name)
        assertEquals(3, response.leader.studyRoom.period1?.floor)
        assertEquals("정보관 302", response.leader.studyRoom.period2?.name)
        assertEquals(2, response.leader.studyRoom.period2?.floor)
    }

    @Test
    fun `개인 심자면 1학년은 학년반 실과 2층을 내려준다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), null), grade = 1, classNo = 2)

        assertEquals("CLASS_1_2", response.leader.studyRoom.period1?.name)
        assertEquals(2, response.leader.studyRoom.period1?.floor)
    }

    @Test
    fun `개인 심자면 2학년은 3층을 내려준다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), null), grade = 2, classNo = 1)

        assertEquals("CLASS_2_1", response.leader.studyRoom.period1?.name)
        assertEquals(3, response.leader.studyRoom.period1?.floor)
    }

    @Test
    fun `개인 심자면 1학년 4반은 3층을 내려준다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), null), grade = 1, classNo = 4)

        assertEquals("CLASS_1_4", response.leader.studyRoom.period1?.name)
        assertEquals(3, response.leader.studyRoom.period1?.floor)
    }

    @Test
    fun `개인 심자면 3학년은 반과 무관하게 GRADE_3와 2층을 내려준다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), null), grade = 3, classNo = 7)

        assertEquals("GRADE_3", response.leader.studyRoom.period1?.name)
        assertEquals(2, response.leader.studyRoom.period1?.floor)
    }

    @Test
    fun `프로젝트 심자인데 방이 배정되지 않으면 null이다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(PeriodAssignment(NightStudyType.PROJECT, null, null), null))

        assertNull(response.leader.studyRoom.period1)
    }

    @Test
    fun `신청하지 않은 차수는 null이다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), null))

        assertNull(response.leader.studyRoom.period2)
    }

    @Test
    fun `참여 정보가 아예 없으면 두 차수 모두 null이다`() {
        val id = UUID.randomUUID()
        val command = command(leaderId = id, memberIds = listOf(id), participations = Participations.EMPTY)
        val leader = listOf(command).toOpenApiNightStudyResponse(userMap(student(id))).single().leader

        assertEquals(NOT_APPLIED, leader.attended.period1)
        assertNull(leader.studyRoom.period1)
        assertNull(leader.studyRoom.period2)
    }

    @Test
    fun `AUTO 심자도 학년반 실로 내려준다`() {
        val id = UUID.randomUUID()
        val auto = PeriodAssignment(NightStudyType.AUTO, null, null)
        val response = single(id, participation(auto, null), grade = 2, classNo = 3)

        assertEquals("CLASS_2_3", response.leader.studyRoom.period1?.name)
        assertEquals(3, response.leader.studyRoom.period1?.floor)
    }

    @Test
    fun `출석 상태는 차수별로 내려간다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(personal(), personal(), ATTENDANCE, ABSENT))

        assertEquals(ATTENDANCE, response.leader.attended.period1)
        assertEquals(ABSENT, response.leader.attended.period2)
    }

    @Test
    fun `실 배정은 학생마다 개별로 내려간다`() {
        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val command = command(
            leaderId = leaderId,
            memberIds = listOf(leaderId, memberId),
            participations = Participations(mapOf(
                leaderId to participation(project("정보관 301", 3), null),
                memberId to participation(personal(), null),
            )),
        )

        val response = listOf(command)
            .toOpenApiNightStudyResponse(userMap(student(leaderId), student(memberId, grade = 2, classNo = 1)))
            .single()

        assertEquals("정보관 301", response.members[0].studyRoom.period1?.name)
        assertEquals("CLASS_2_1", response.members[1].studyRoom.period1?.name)
    }

    @Test
    fun `리더 정보를 찾지 못하면 해당 심자를 응답에서 제외한다`() {
        val id = UUID.randomUUID()
        val command = command(
            leaderId = id,
            memberIds = listOf(id),
            participations = Participations(mapOf(id to participation(personal(), null))),
        )

        assertTrue(listOf(command).toOpenApiNightStudyResponse(emptyMap()).isEmpty())
    }

    @Test
    fun `리더가 없는 심자는 응답에서 제외한다`() {
        val command = command(leaderId = null, memberIds = emptyList(), participations = Participations.EMPTY)

        assertTrue(listOf(command).toOpenApiNightStudyResponse(emptyMap()).isEmpty())
    }

    @Test
    fun `attended와 studyRoom은 차수 필드를 가진 객체로 직렬화된다`() {
        val id = UUID.randomUUID()
        val response = single(id, participation(project("정보관 301", 3), null, ATTENDANCE, NOT_APPLIED))

        val json = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule()).writeValueAsString(response)

        assertTrue(json.contains("\"attended\":{\"period1\":\"ATTENDANCE\",\"period2\":\"NOT_APPLIED\"}"), json)
        assertTrue(
            json.contains("\"studyRoom\":{\"period1\":{\"name\":\"정보관 301\",\"floor\":3},\"period2\":null}"),
            json,
        )
    }

    private fun single(
        userId: UUID,
        participation: Participation,
        grade: Int = 1,
        classNo: Int = 2,
    ) = listOf(
        command(
            leaderId = userId,
            memberIds = listOf(userId),
            participations = Participations(mapOf(userId to participation)),
        )
    ).toOpenApiNightStudyResponse(userMap(student(userId, grade, classNo))).single()

    private fun project(name: String, floor: Int) =
        PeriodAssignment(NightStudyType.PROJECT, name, floor)

    private fun personal() = PeriodAssignment(NightStudyType.PERSONAL, null, null)

    private fun participation(
        period1: PeriodAssignment?,
        period2: PeriodAssignment?,
        attended1: NightStudyAttendanceStatus = ATTENDANCE,
        attended2: NightStudyAttendanceStatus = NOT_APPLIED,
    ) = Participation(
        attendance = Attendance(period1 = attended1, period2 = attended2),
        assignment = Assignment(period1 = period1, period2 = period2),
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

    private fun student(userId: UUID, grade: Int = 1, classNo: Int = 2): UserResponse = UserResponse.newBuilder()
        .setPublicId(userId.toString())
        .setName("학생")
        .setGender("MALE")
        .setStudent(StudentInfo.newBuilder().setGrade(grade).setRoom(classNo).setNumber(3))
        .build()

    private fun userMap(vararg users: UserResponse): Map<String?, UserResponse> =
        users.associateBy { it.publicId }
}
