package com.b1nd.dodamdodam.nightstudy.domain.room.exception

import com.b1nd.dodamdodam.core.common.exception.BasicException

class RoomNotFoundException : BasicException(RoomExceptionCode.ROOM_NOT_FOUND)
class RoomAlreadyExistsException : BasicException(RoomExceptionCode.ROOM_ALREADY_EXISTS)
