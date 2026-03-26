package com.b1nd.dodamdodam.nightstudy.application.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.nightstudy.application.room.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.room.data.toResponseList
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomResponse
import com.b1nd.dodamdodam.nightstudy.domain.room.service.RoomService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class RoomUseCase(
    private val roomService: RoomService,
) {

    fun create(request: SaveRoomRequest): Response<Any> {
        roomService.save(request.toEntity())
        return Response.created("방을 생성했어요.")
    }

    fun getAll(): Response<List<RoomResponse>> {
        val rooms = roomService.getAll().toResponseList()
        return Response.ok("방 목록을 조회했어요.", rooms)
    }

    fun update(id: UUID, request: SaveRoomRequest): Response<Any> {
        roomService.update(id, request.name)
        return Response.ok("방 정보를 수정했어요.")
    }

    fun delete(id: UUID): Response<Any> {
        roomService.delete(id)
        return Response.ok("방을 삭제했어요.")
    }
}
