package com.b1nd.dodamdodam.weekendstudy.domain.attendance.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class WeekendStudyAttendanceExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦어요."),
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "체크된 출결이 없어요."),
}
