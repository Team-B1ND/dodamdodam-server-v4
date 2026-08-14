package com.b1nd.dodamdodam.nightstudy.application.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomDetailResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomMemberResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomSummaryResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.room.data.toResponseList
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyRoomMemberCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.InvalidNightStudyTypeException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.ProjectRoomNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class ProjectRoomUseCase(
    private val projectRoomService: ProjectRoomService,
    private val nightStudyService: NightStudyService,
    private val nightStudyAttendanceService: NightStudyAttendanceService,
    private val userQueryClient: UserQueryClient,
) {

    companion object {
        private const val THIRD_GRADE_ROOM_ID = "GRADE_3"
        private const val THIRD_GRADE_ROOM_NAME = "3학년 심자실"
        private const val MAX_CLASS_ROOM = 4
    }

    fun create(request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.save(request.toEntity())
        return Response.created("방을 생성했어요.")
    }

    @Transactional(readOnly = true)
    fun getAll(): Response<List<ProjectRoomResponse>> {
        val inUsePeriods = projectRoomService.getInUsePeriods()
        val rooms = projectRoomService.getAll().toResponseList(inUsePeriods)
        return Response.ok("방 목록을 조회했어요.", rooms)
    }

    @Transactional(readOnly = true)
    fun getAllWithStatus(date: LocalDate, period: Int): Response<List<RoomSummaryResponse>> {
        val snapshot = createSnapshot(date, period)
        val rooms = snapshot.rooms.map { room ->
            val members = snapshot.membersByRoom[room.id].orEmpty()
            RoomSummaryResponse(
                roomId = room.id,
                roomName = room.name,
                memberCount = members.size,
                unchecked = members.count { !it.attended },
            )
        }

        return Response.ok("심야자습 실별 현황을 조회했어요.", rooms)
    }

    @Transactional(readOnly = true)
    fun getByRoomId(roomId: String, date: LocalDate, period: Int): Response<RoomDetailResponse> {
        val snapshot = createSnapshot(date, period)
        if (snapshot.rooms.none { it.id == roomId }) throw ProjectRoomNotFoundException()

        return Response.ok(
            "심야자습 실 인원을 조회했어요.",
            RoomDetailResponse(roomMember = snapshot.membersByRoom[roomId].orEmpty()),
        )
    }

    fun update(id: Long, request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.update(id, request.name)
        return Response.ok("방 정보를 수정했어요.")
    }

    fun delete(id: Long): Response<Any> {
        projectRoomService.delete(id)
        return Response.ok("방을 삭제했어요.")
    }

    private fun createSnapshot(date: LocalDate, period: Int): RoomSnapshot {
        validatePeriod(period)

        val rooms = getRooms()
        val validRoomIds = rooms.map { it.id }.toSet()
        val assignments = nightStudyService.getAllowedRoomMembers(date, period)
            .groupBy { it.userId }
            .mapValues { (_, values) -> selectAssignment(values, period) }
        val attendedUserIds = nightStudyAttendanceService.getAttendedUserIds(assignments.keys, date, period)
        val users = if (assignments.isEmpty()) {
            emptyList()
        } else {
            runBlocking { userQueryClient.getUsers(assignments.keys.map(UUID::toString)) }.usersList
        }

        val membersByRoom = users.mapNotNull { user ->
            val userId = UUID.fromString(user.publicId)
            val assignment = assignments[userId] ?: return@mapNotNull null
            if (!user.hasStudent()) return@mapNotNull null
            val student = user.student
            val roomId = resolveRoomId(assignment, student.grade, student.room)
                ?.takeIf { it in validRoomIds }
                ?: return@mapNotNull null

            roomId to RoomMemberResponse(
                userId = userId,
                name = user.name,
                profileImage = user.profileImage.takeIf { it.isNotBlank() },
                grade = student.grade,
                room = student.room,
                number = student.number,
                attended = userId in attendedUserIds,
            )
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        ).mapValues { (_, members) ->
            members.sortedWith(compareBy(RoomMemberResponse::grade, RoomMemberResponse::room, RoomMemberResponse::number))
        }

        return RoomSnapshot(rooms = rooms, membersByRoom = membersByRoom)
    }

    private fun getRooms(): List<RoomDefinition> {
        val classRooms = (1..2).flatMap { grade ->
            (1..MAX_CLASS_ROOM).map { room ->
                RoomDefinition(id = "CLASS_${grade}_$room", name = "${grade}학년 ${room}반")
            }
        }
        val thirdGradeRoom = RoomDefinition(id = THIRD_GRADE_ROOM_ID, name = THIRD_GRADE_ROOM_NAME)
        val projectRooms = projectRoomService.getAll()
            .sortedBy { it.name }
            .map { RoomDefinition(id = "PROJECT_${it.id!!}", name = it.name) }

        return classRooms + thirdGradeRoom + projectRooms
    }

    private fun selectAssignment(
        assignments: List<NightStudyRoomMemberCommand>,
        period: Int,
    ): NightStudyRoomMemberCommand {
        val periodAssignments = assignments.filter { it.period == period }.ifEmpty { assignments }

        return periodAssignments.firstOrNull {
            it.type == NightStudyType.PROJECT && it.projectRoomId != null
        } ?: periodAssignments.firstOrNull { it.type == NightStudyType.PERSONAL }
        ?: periodAssignments.first()
    }

    private fun resolveRoomId(assignment: NightStudyRoomMemberCommand, grade: Int, room: Int): String? {
        if (assignment.type == NightStudyType.PROJECT && assignment.projectRoomId != null) {
            return "PROJECT_${assignment.projectRoomId}"
        }

        return when (grade) {
            1, 2 -> "CLASS_${grade}_$room"
            3 -> THIRD_GRADE_ROOM_ID
            else -> null
        }
    }

    private fun validatePeriod(period: Int) {
        if (period !in 1..2) throw InvalidNightStudyTypeException()
    }

    private data class RoomDefinition(
        val id: String,
        val name: String,
    )

    private data class RoomSnapshot(
        val rooms: List<RoomDefinition>,
        val membersByRoom: Map<String, List<RoomMemberResponse>>,
    )
}
