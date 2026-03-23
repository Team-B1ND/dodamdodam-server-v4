package com.b1nd.dodamdodam.neis.domain.schedule.exception

import com.b1nd.dodamdodam.core.common.exception.BasicException

class ScheduleNotFoundException : BasicException(ScheduleExceptionCode.SCHEDULE_NOT_FOUND)
