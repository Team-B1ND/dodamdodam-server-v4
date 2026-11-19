package com.b1nd.dodamdodam.weekendstudy.domain.attendance.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class WeekendStudyAttendanceExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    NOT_ALLOWED_WEEKEND_STUDY(HttpStatus.BAD_REQUEST, "승인된 주말 자습만 출석을 체크할 수 있어요."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦어요."),
}
