package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.PersonalWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.ProjectWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.RejectWeekendStudyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.PersonalWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.ProjectWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicantResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicationResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toApplicantResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toApplicationResponses
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toEntity
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toPersonalResponses
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toProjectResponses
import com.b1nd.dodamdodam.weekendstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.service.WeekendStudyDeadlineService
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomBlockService
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.EmptyPeriodException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.InvalidMemberException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.InvalidPeriodException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotApplicationTimeException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotOperatingDayException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service.WeekendStudyService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class WeekendStudyUseCase(
    private val weekendStudyService: WeekendStudyService,
    private val deadlineService: WeekendStudyDeadlineService,
    private val projectRoomService: ProjectRoomService,
    private val projectRoomBlockService: ProjectRoomBlockService,
    private val userQueryClient: UserQueryClient,
) {
    companion object {
        private val AVAILABLE_PERIODS = setOf(17, 18, 19, 20)
    }

    fun applyPersonal(request: PersonalWeekendStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        validateApplication(request.date, request.periods)
        weekendStudyService.save(request.toEntity(userId), request.periods)
        return Response.created("개별 자습 신청이 완료됐어요.")
    }

    fun applyProject(request: ProjectWeekendStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        validateApplication(request.date, request.periods)
        validateMembers(userId, request.members)
        val wishRoom = request.wishRoomId?.let { projectRoomService.getById(it) }
        wishRoom?.let { projectRoomBlockService.validateNotBlocked(it, request.date, request.periods) }
        weekendStudyService.save(request.toEntity(userId, wishRoom), request.periods, request.members)
        return Response.created("프로젝트 자습 신청이 완료됐어요.")
    }

    @Transactional(readOnly = true)
    fun getMyPersonal(): Response<List<PersonalWeekendStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val periods = weekendStudyService.getAllMyPeriods(userId, WeekendStudyType.PERSONAL, LocalDate.now())
        return Response.ok("개별 자습 신청 내역을 조회했어요.", periods.toPersonalResponses())
    }

    @Transactional(readOnly = true)
    fun getMyProject(): Response<List<ProjectWeekendStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val periods = weekendStudyService.getAllMyPeriods(userId, WeekendStudyType.PROJECT, LocalDate.now())
        return Response.ok("프로젝트 자습 신청 내역을 조회했어요.", periods.toProjectResponses(userId))
    }

    @Transactional(readOnly = true)
    fun getApplications(date: LocalDate, type: WeekendStudyType, pageable: Pageable): Response<InfinityScrollPageResponse<WeekendStudyApplicationResponse>> {
        val periods = weekendStudyService.getAllPeriods(date, type)
        val usersMap = fetchUsersMap(periods.map { it.userId })
        val applications = periods.toApplicationResponses(usersMap)

        val sorted = if (type == WeekendStudyType.PERSONAL) {
            applications.sortedWith(compareBy(
                { it.leader.student?.grade ?: Int.MAX_VALUE },
                { it.leader.student?.room ?: Int.MAX_VALUE },
                { it.leader.student?.number ?: Int.MAX_VALUE },
            ))
        } else {
            applications
        }

        val offset = pageable.offset.toInt()
        val pageSize = pageable.pageSize
        val sliced = sorted.drop(offset).take(pageSize)
        val hasNext = offset + pageSize < sorted.size

        return Response.ok(
            "주말 자습 신청 목록을 조회했어요.",
            InfinityScrollPageResponse(content = sliced, hasNext = hasNext)
        )
    }

    fun allow(id: UUID): Response<Any> {
        weekendStudyService.allow(id)
        return Response.ok("주말 자습 신청을 승인했어요.")
    }

    fun reject(id: UUID, request: RejectWeekendStudyRequest): Response<Any> {
        weekendStudyService.reject(id, request.rejectionReason)
        return Response.ok("주말 자습 신청을 거절했어요.")
    }

    private fun validateApplication(date: LocalDate, periods: List<Int>) {
        if (date.dayOfWeek != DayOfWeek.SUNDAY)
            throw NotOperatingDayException()
        if (date != LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)))
            throw NotApplicationTimeException()
        deadlineService.validateDeadline()
        if (periods.isEmpty())
            throw EmptyPeriodException()
            if (!AVAILABLE_PERIODS.containsAll(periods) || periods.size != periods.toSet().size)
            throw InvalidPeriodException()
    }

    private fun fetchUsersMap(userIds: List<UUID>): Map<String, WeekendStudyApplicantResponse> {
        val ids = userIds.distinct().map { it.toString() }
        if (ids.isEmpty()) return emptyMap()
        return runBlocking { userQueryClient.getUsers(ids) }.usersList
            .associate { it.publicId to it.toApplicantResponse() }
    }

    private fun validateMembers(leaderId: UUID, members: List<UUID>) {
        if (leaderId in members || members.size != members.toSet().size)
            throw InvalidMemberException()
    }
}
