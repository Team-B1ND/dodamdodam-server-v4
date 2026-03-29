package com.b1nd.dodamdodam.nightstudy.application.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.room.data.toResponseList
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(rollbackFor = [Exception::class])
class ProjectRoomUseCase(
    private val projectRoomService: ProjectRoomService,
) {

    fun create(request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.save(request.toEntity())
        return Response.created("방을 생성했어요.")
    }

    @Transactional(readOnly = true)
    fun getAll(): Response<List<ProjectRoomResponse>> {
        val rooms = projectRoomService.getAll().toResponseList()
        return Response.ok("방 목록을 조회했어요.", rooms)
    }

    fun update(id: Long, request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.update(id, request.name)
        return Response.ok("방 정보를 수정했어요.")
    }

    fun delete(id: Long): Response<Any> {
        projectRoomService.delete(id)
        return Response.ok("방을 삭제했어요.")
    }
}
