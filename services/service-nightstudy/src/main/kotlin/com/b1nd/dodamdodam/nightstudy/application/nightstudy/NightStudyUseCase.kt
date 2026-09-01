package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.*
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.UpdateNightStudyAttendanceRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.*
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.outSleeping.client.OutSleepingClient
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyTotalCountResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.InvalidNightStudyTypeException
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
@Transactional(rollbackFor = [Exception::class])
class NightStudyUseCase(
    private val nightStudyService: NightStudyService,
    private val nightStudyAttendanceService: NightStudyAttendanceService,
    private val projectRoomService: ProjectRoomService,
    private val userQueryClient: UserQueryClient,
    private val outSleepingClient: OutSleepingClient
) {

    companion object {
        private const val DEFAULT_FLOOR = 2
        private const val MAX_PERIOD = 2
        private const val MIN_GRADE = 1
        private const val MAX_GRADE = 3
    }

    fun applyPersonalNightStudy(request: PersonalNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        validateApplicationAvailability(request.startAt, request.period)
        nightStudyService.save(request.toEntity(), userId, null)
        return Response.created("개인 심자 신청이 완료됐어요.")
    }

    fun applyProjectNightStudy(request: ProjectNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        validateApplicationAvailability(request.startAt, request.period)
        val wishRoom = request.wishRoomId?.let { projectRoomService.getById(it) }
        nightStudyService.save(request.toEntity(wishRoom), userId, request.members)
        return Response.created("프로젝트 심자 신청이 완료됐어요.")
    }

    fun getMyPersonalNightStudy(): Response<List<PersonalNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val results = nightStudyService.getAllByUserIdAndType(userId, NightStudyType.PERSONAL)
        return Response.ok("개인 심자 신청 목록을 조회했어요.", results.toPersonalNightStudyListResponse())
    }

    fun getMyProjectNightStudy(): Response<List<ProjectNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val results = nightStudyService.getAllByUserIdAndType(userId, NightStudyType.PROJECT)
        val leaderMap = nightStudyService.getLeadersByNightStudies(results)
        val responses = results.map { nightStudy ->
            nightStudy.toProjectNightStudyResponse(isLeader = leaderMap[nightStudy.id] == userId)
        }
        return Response.ok("프로젝트 심자 신청 목록을 조회했어요.", responses)
    }

    fun cancelNightStudy(id: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.delete(userId, id)
        return Response.ok("심자 신청을 취소했어요.")
    }

    fun searchAllByType(type: NightStudyType, keyword: String?, status: NightStudyStatusType?, pageable: Pageable): Response<InfinityScrollPageResponse<NightStudyApplicationResponse>> {
        val userIds = keyword?.takeIf { it.isNotBlank() }?.let {
            runBlocking { userQueryClient.getUsersByNameKeyword(it) }
                .usersList.map { user -> UUID.fromString(user.publicId) }
                .ifEmpty { return Response.ok("전체 심야자습 신청 목록을 조회했어요.", InfinityScrollPageResponse(emptyList(), false)) }
        }

        val allNightStudies = nightStudyService.searchByType(type, userIds, status)
        val nightStudiesWithMembers = getNightStudiesWithMembersAndLeaders(allNightStudies)

        val leaderIds = nightStudiesWithMembers.mapNotNull { it.leaderId }.distinct()
        val outSleepingUserIds = fetchOngoingOutSleepingUserIds(leaderIds)
        val visibleNightStudies = nightStudiesWithMembers.filter { it.leaderId !in outSleepingUserIds }

        val usersMap = fetchUsersMap(visibleNightStudies)
        val projectMemberNightStudyIds = nightStudyService.getProjectMemberNightStudyIds(allNightStudies)

        val sorted = if (type == NightStudyType.PROJECT) {
            visibleNightStudies.sortedWith(
                compareBy<NightStudyWithMembersCommand> {
                    it.nightStudy.createdAt ?: LocalDateTime.MAX
                }.thenBy {
                    it.nightStudy.id ?: Long.MAX_VALUE
                }
            )
        } else {
            visibleNightStudies.sortedWith(compareBy(
                { usersMap[it.leaderId?.toString()]?.student?.grade ?: Int.MAX_VALUE },
                { usersMap[it.leaderId?.toString()]?.student?.room ?: Int.MAX_VALUE },
                { usersMap[it.leaderId?.toString()]?.student?.number ?: Int.MAX_VALUE }
            ))
        }

        val offset = pageable.offset.toInt()
        val pageSize = pageable.pageSize
        val sliced = sorted.drop(offset).take(pageSize)
        val hasNext = offset + pageSize < sorted.size

        val responses = sliced.toNightStudyApplicationResponses(usersMap, projectMemberNightStudyIds)

        return Response.ok(
            "전체 심야자습 신청 목록을 조회했어요.",
            InfinityScrollPageResponse(content = responses, hasNext = hasNext)
        )
    }

    fun countApproved(): Response<NightStudyApprovedCountResponse> {
        val counts = nightStudyService.countAllowedMembersGroupByTypeAndPeriod()
        val byType = NightStudyType.entries.associateWith { type ->
            NightStudyApprovedCountResponse.PeriodCount(
                period1 = counts[type to 1] ?: 0,
                period2 = counts[type to 2] ?: 0,
            )
        }
        return Response.ok(
            "심자 승인 인원수를 조회했어요.",
            NightStudyApprovedCountResponse(
                personal = byType.getValue(NightStudyType.PERSONAL),
                project = byType.getValue(NightStudyType.PROJECT),
                total = NightStudyApprovedCountResponse.PeriodCount(
                    period1 = byType.values.sumOf { it.period1 } - byType.getValue(NightStudyType.AUTO).period1,
                    period2 = byType.values.sumOf { it.period2 } - byType.getValue(NightStudyType.AUTO).period2,
                ),
            ),
        )
    }

    fun findById(id: UUID): Response<NightStudyApplicationResponse> {
        val nightStudy = nightStudyService.getByPublicId(id)
        val memberIds = nightStudyService.getMembersByNightStudy(nightStudy)
        val leaderId = nightStudyService.getLeaderByNightStudy(nightStudy)

        val usersMap = fetchUsersMapForSingleNightStudy(leaderId, memberIds)
        val response = nightStudy.toNightStudyApplicationDetailResponse(leaderId, memberIds, usersMap)

        return Response.ok("심자 신청을 조회했어요.", response)
    }

    fun allow(id: UUID): Response<Any> {
        nightStudyService.allow(id)
        return Response.ok("심자 신청을 승인했어요.")
    }

    fun reject(id: UUID, rejectionReason: String): Response<Any> {
        nightStudyService.reject(id, rejectionReason)
        return Response.ok("심자 신청을 거절했어요.")
    }

    fun pending(id: UUID): Response<Any> {
        nightStudyService.pending(id)
        return Response.ok("심자 신청을 대기 상태로 변경했어요.")
    }

    fun assignRoom(id: UUID, roomId: Long): Response<Any> {
        val room = projectRoomService.getById(roomId)
        nightStudyService.assignRoom(id, room)
        return Response.ok("방 배정이 완료됐어요.")
    }

    fun countTotalMembers(): Response<NightStudyTotalCountResponse> {
        val nightStudies = nightStudyService.getAllAllowed()
        val allMembersByNightStudy = nightStudyService.getMembersByNightStudies(nightStudies)
        val userIds = allMembersByNightStudy.values.flatten().distinct()

        val outSleepingUserIds = fetchOngoingOutSleepingUserIds(userIds)
        val membersByNightStudy = allMembersByNightStudy
            .mapValues { (_, memberIds) -> memberIds.filterNot { it in outSleepingUserIds } }
        val userById = fetchUsersById(userIds - outSleepingUserIds)

        val members = (1..MAX_PERIOD).flatMap { period ->
            representativesByUser(nightStudies, membersByNightStudy, period)
                .mapNotNull { (userId, nightStudy) -> resolveMemberCount(userById[userId], nightStudy, period) }
        }

        return Response.ok("심자 인원수를 조회했어요.", NightStudyTotalCountResponse.of(members))
    }

    private fun representativesByUser(
        nightStudies: List<NightStudyEntity>,
        membersByNightStudy: Map<Long, List<UUID>>,
        period: Int
    ): Map<UUID, NightStudyEntity> {
        return nightStudies
            .filter { it.period in period..MAX_PERIOD }
            .flatMap { nightStudy ->
                membersByNightStudy[nightStudy.id].orEmpty().map { userId -> userId to nightStudy }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, covering) -> covering.maxBy { priorityOf(it, period) } }
    }

    private fun priorityOf(nightStudy: NightStudyEntity, period: Int): Int =
        (if (nightStudy.type == NightStudyType.PROJECT) 2 else 0) +
            (if (nightStudy.period == period) 1 else 0)

    private fun resolveMemberCount(
        user: UserResponse?,
        nightStudy: NightStudyEntity,
        period: Int
    ): NightStudyTotalCountResponse.MemberCount? {
        if (user == null || !user.hasStudent()) return null
        val grade = user.student.grade.takeIf { it in MIN_GRADE..MAX_GRADE } ?: return null

        return NightStudyTotalCountResponse.MemberCount(
            floor = resolveFloor(nightStudy, user),
            grade = grade,
            gender = user.gender,
            period = period,
            type = nightStudy.type,
        )
    }

    private fun fetchUsersById(userIds: List<UUID>): Map<UUID, UserResponse> {
        if (userIds.isEmpty()) return emptyMap()

        return runBlocking { userQueryClient.getUsers(userIds.map(UUID::toString)) }
            .usersList.associateBy { UUID.fromString(it.publicId) }
    }

    private fun resolveFloor(nightStudy: NightStudyEntity, user: UserResponse?): Int =
        if (nightStudy.type == NightStudyType.PROJECT) {
            nightStudy.room?.floor ?: DEFAULT_FLOOR
        } else {
            val grade = user?.student?.grade
            val classNo = user?.student?.room
            if (grade == 2 || (grade == 1 && classNo == 4)) 3 else DEFAULT_FLOOR
        }

    fun unassignRoom(id: UUID): Response<Any> {
        nightStudyService.unassignRoom(id)
        return Response.ok("방 배정을 해제했어요.")
    }

    fun updateAttendance(
        userId: UUID,
        date: LocalDate,
        period: Int,
        request: UpdateNightStudyAttendanceRequest
    ): Response<NightStudyAttendanceResponse> {
        val attendance = nightStudyAttendanceService.update(userId, date, period, request.attended)
        return Response.ok("심야자습 출석 상태를 변경했어요.", NightStudyAttendanceResponse.from(attendance))
    }

    fun getAttendance(userId: UUID, date: LocalDate, period: Int): Response<NightStudyAttendanceResponse> {
        val attendance = nightStudyAttendanceService.get(userId, date, period)
        return Response.ok("심야자습 출석 상태를 조회했어요.", NightStudyAttendanceResponse.from(attendance))
    }

    fun countAttendance(date: LocalDate, period: Int): Response<NightStudyAttendanceCountResponse> {
        val (attendedCount, absentCount) = nightStudyAttendanceService.count(date, period)
        return Response.ok(
            "심야자습 출석 인원수를 조회했어요.",
            NightStudyAttendanceCountResponse(
                attendedCount = attendedCount,
                absentCount = absentCount,
            )
        )
    }

    private fun getNightStudiesWithMembersAndLeaders(
        nightStudies: List<NightStudyEntity>
    ): List<NightStudyWithMembersCommand> {
        val leaderMap = nightStudyService.getLeadersByNightStudies(nightStudies)
        val membersMap = nightStudyService.getMembersByNightStudies(nightStudies)

        return nightStudies.map { nightStudy ->
            NightStudyWithMembersCommand(
                nightStudy = nightStudy,
                leaderId = leaderMap[nightStudy.id],
                memberIds = membersMap[nightStudy.id] ?: emptyList(),
                attendedUserIds = emptySet(),
            )
        }
    }

    private fun fetchOngoingOutSleepingUserIds(userIds: List<UUID>): Set<UUID> {
        if (userIds.isEmpty()) return emptySet()

        val today = LocalDate.now()
        return runBlocking { outSleepingClient.getOutSleeping(userIds) }
            .outSleepingsList
            .filter { outSleeping ->
                outSleeping.status == "ALLOWED" &&
                    !today.isBefore(LocalDate.parse(outSleeping.startAt)) &&
                    !today.isAfter(LocalDate.parse(outSleeping.endAt))
            }
            .map { UUID.fromString(it.userId) }
            .toSet()
    }

    private fun fetchUsersMap(
        nightStudiesWithMembers: List<NightStudyWithMembersCommand>
    ): Map<String, NightStudyApplicantResponse> {
        val allUserIds = nightStudiesWithMembers
            .flatMap { dto -> listOfNotNull(dto.leaderId) + dto.memberIds }
            .map { it.toString() }
            .distinct()

        return if (allUserIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(allUserIds) }.usersList
                .associate { it.publicId to it.toOpenApiUserInfoResponse() }
        } else {
            emptyMap()
        }
    }

    private fun fetchUsersMapForSingleNightStudy(
        leaderId: UUID?,
        memberIds: List<UUID>
    ): Map<String, NightStudyApplicantResponse> {
        val userIds = (listOfNotNull(leaderId) + memberIds).map { it.toString() }
        return runBlocking { userQueryClient.getUsers(userIds) }.usersList
            .associate { it.publicId to it.toOpenApiUserInfoResponse() }
    }

    private fun validateApplicationAvailability(stratAt: LocalDate, period: Int) {
        val zone = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(zone)
        val deadline = LocalDate.now(zone).atTime(20, 30)
        if (now.isAfter(deadline))
            throw BasicException(NightStudyExceptionCode.NOT_APPLICATION_TIME)
        if (stratAt.isBefore(now.toLocalDate()))
            throw BasicException(NightStudyExceptionCode.INVALID_START_AT)
        if (period > 2 || period < 1)
            throw InvalidNightStudyTypeException()
    }
}
