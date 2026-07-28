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
    BAN_NOT_FOUND(HttpStatus.NOT_FOUND, "정지된 인원을 찾을 수 없어요."),
    ALREADY_BANNED(HttpStatus.CONFLICT, "이미 정지된 인원이에요."),
    NOT_PROJECT_NIGHT_STUDY(HttpStatus.BAD_REQUEST, "프로젝트 심야 자습에만 방을 배정할 수 있어요."),
    ROOM_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "해당 기간에 이미 배정된 방이에요."),
    NOT_APPLICATION_TIME(HttpStatus.BAD_REQUEST, "지금은 심자 신청 시간이 아니에요."),
    INVALID_START_AT(HttpStatus.BAD_REQUEST, "현재 시간보다 시작일이 과거에요."),
    ALREADY_APPROVED(HttpStatus.CONFLICT, "이미 승인된 심야 자습이에요."),
    NOT_NIGHT_STUDY_MEMBER(HttpStatus.BAD_REQUEST, "해당 날짜와 교시에 승인된 심야 자습 인원이 아니에요."),
    INVALID_NIGHT_STUDY_TYPE(HttpStatus.BAD_REQUEST, "심야 자습 신청은 1교시 또는 2교시만 신청할 수 있어요."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없어요."),
    NOT_TEAM_OWNER(HttpStatus.FORBIDDEN, "당신은 팀의 주인이 아니에요."),
    TEAM_INVITEE_NOT_FOUND(HttpStatus.NOT_FOUND, "초대할 유저를 찾을 수 없어요."),
    TEAM_INVITATION_NOT_RECEIVED(HttpStatus.BAD_REQUEST, "팀 초대를 받지 않았어요."),
    ALREADY_JOINED_TEAM(HttpStatus.CONFLICT, "이미 가입되어 있는 팀이에요."),
    NOT_JOINED_TEAM(HttpStatus.BAD_REQUEST, "가입되어 있지 않은 팀이에요."),

    ;
}
