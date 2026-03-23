package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class NightStudyExceptionCode(
    override val status: HttpStatus,
    override val message: String
): ExceptionCode {
    NIGHT_STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "심야 자습 신청을 찾을 수 없어요."),
    NIGHT_STUDY_BANNED(HttpStatus.FORBIDDEN, "심야 자습이 정지된 인원이 있어요."),
    NOT_MY_NIGHT_STUDY(HttpStatus.FORBIDDEN, "내가 신청한 심야 자습이 아니에요."),
    NOT_LEADER(HttpStatus.FORBIDDEN, "프로젝트 심야 자습은 리더만 삭제할 수 있어요."),
    PERIOD_OVERLAPPED(HttpStatus.BAD_REQUEST, "이미 해당 기간에 신청한 심야 자습이 있어요."),
    ;
}
