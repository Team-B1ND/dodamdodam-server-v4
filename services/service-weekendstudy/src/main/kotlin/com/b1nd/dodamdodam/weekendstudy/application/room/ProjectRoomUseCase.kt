package com.b1nd.dodamdodam.weekendstudy.application.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.BlockProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.weekendstudy.application.room.data.toEntities
import com.b1nd.dodamdodam.weekendstudy.application.room.data.toEntity
import com.b1nd.dodamdodam.weekendstudy.application.room.data.toResponse
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.InvalidBlockDateRangeException
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomBlockService
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.EmptyPeriodException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.InvalidPeriodException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
@Transactional(rollbackFor = [Exception::class])
class ProjectRoomUseCase(
    private val projectRoomService: ProjectRoomService,
    private val projectRoomBlockService: ProjectRoomBlockService,
) {
    companion object {
        private val AVAILABLE_PERIODS = setOf(17, 18, 19, 20, 21)
    }

    fun create(request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.save(request.toEntity())
        return Response.created("방을 생성했어요.")
    }

    @Transactional(readOnly = true)
    fun getAll(): Response<List<ProjectRoomResponse>> =
        Response.ok("방 목록을 조회했어요.", projectRoomService.getAll().map { it.toResponse() })

    fun update(id: Long, request: SaveProjectRoomRequest): Response<Any> {
        projectRoomService.update(id, request.name, request.floor)
        return Response.ok("방을 수정했어요.")
    }

    fun delete(id: Long): Response<Any> {
        projectRoomService.delete(id)
        return Response.ok("방을 삭제했어요.")
    }

    fun block(roomId: Long, request: BlockProjectRoomRequest): Response<Any> {
        val room = projectRoomService.getById(roomId)
        validateBlock(request.startAt, request.endAt, request.periods)
        projectRoomBlockService.saveAll(request.toEntities(room))
        return Response.created("방을 차단했어요.")
    }

    private fun validateBlock(startAt: LocalDate, endAt: LocalDate, periods: List<Int>) {
        if (startAt.isAfter(endAt))
            throw InvalidBlockDateRangeException()
        if (periods.isEmpty())
            throw EmptyPeriodException()
        if (!AVAILABLE_PERIODS.containsAll(periods) || periods.size != periods.toSet().size)
            throw InvalidPeriodException()
    }
}
