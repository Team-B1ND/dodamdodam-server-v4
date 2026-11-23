package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class WeekendStudyExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    NOT_OPERATING_DAY(HttpStatus.BAD_REQUEST, "주말 자습을 운영하지 않는 날이에요."),
    NOT_APPLICATION_TIME(HttpStatus.BAD_REQUEST, "신청이 마감되었어요. 변경은 관리자에게 문의해주세요."),
    EMPTY_PERIOD(HttpStatus.BAD_REQUEST, "시간을 1개 이상 선택해주세요."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "올바르지 않은 교시예요."),
    PERIOD_OVERLAPPED(HttpStatus.CONFLICT, "이미 신청한 시간이에요."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST, "팀원 구성이 올바르지 않아요."),
    WEEKEND_STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "주말 자습 신청을 찾을 수 없어요."),
    WEEKEND_STUDY_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 교시에 신청한 주말 자습이 없어요."),
    INVALID_WEEKEND_STUDY_TYPE(HttpStatus.BAD_REQUEST, "주말 자습 신청 유형이 맞지 않아요."),
    ROOM_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 시간이에요."),
    NOT_MY_WEEKEND_STUDY(HttpStatus.FORBIDDEN, "본인이 신청한 주말 자습이 아니에요."),
    NOT_LEADER(HttpStatus.FORBIDDEN, "프로젝트 리더만 수정·취소할 수 있어요."),
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.CONFLICT, "출결이 체크된 주말 자습은 변경할 수 없어요."),
}
