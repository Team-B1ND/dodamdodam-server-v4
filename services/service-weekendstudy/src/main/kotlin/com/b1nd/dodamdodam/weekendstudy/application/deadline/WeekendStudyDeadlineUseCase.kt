package com.b1nd.dodamdodam.weekendstudy.application.deadline

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.request.UpdateDeadlineRequest
import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.response.DeadlineResponse
import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.toResponse
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.service.WeekendStudyDeadlineService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(rollbackFor = [Exception::class])
class WeekendStudyDeadlineUseCase(
    private val deadlineService: WeekendStudyDeadlineService,
) {
    @Transactional(readOnly = true)
    fun getDeadline(): Response<DeadlineResponse> {
        val deadline = deadlineService.get()
        return Response.ok("주말 자습 신청 마감 시간을 조회했어요.", deadline?.toResponse())
    }

    fun updateDeadline(request: UpdateDeadlineRequest): Response<Any> {
        deadlineService.update(request.startDayOfWeek, request.startTime, request.endDayOfWeek, request.endTime)
        return Response.ok("주말 자습 신청 마감 시간이 수정되었어요.")
    }
}
