package com.b1nd.dodamdodam.neis.domain.schedule.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class ScheduleExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "학사일정을 찾을 수 없어요."),
    ;
}
