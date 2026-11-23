package com.b1nd.dodamdodam.weekendstudy.presentation.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.room.ProjectRoomUseCase
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.BlockProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomBlockResponse
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomStatusResponse
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@RestController
@RequestMapping("/rooms")
class ProjectRoomController(
    private val projectRoomUseCase: ProjectRoomUseCase,
) {
    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PostMapping
    fun create(@RequestBody @Valid request: SaveProjectRoomRequest): Response<Any> =
        projectRoomUseCase.create(request)

    @UserAccess
    @GetMapping
    fun getAll(): Response<List<ProjectRoomResponse>> =
        projectRoomUseCase.getAll()

    @UserAccess
    @GetMapping("/status")
    fun getStatus(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam(required = false) floor: Int?,
    ): Response<List<ProjectRoomStatusResponse>> =
        projectRoomUseCase.getStatus(date ?: LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)), floor)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: SaveProjectRoomRequest): Response<Any> =
        projectRoomUseCase.update(id, request)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Response<Any> =
        projectRoomUseCase.delete(id)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PostMapping("/{roomId}/blocks")
    fun block(@PathVariable roomId: Long, @RequestBody @Valid request: BlockProjectRoomRequest): Response<Any> =
        projectRoomUseCase.block(roomId, request)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @GetMapping("/{roomId}/blocks")
    fun getBlocks(@PathVariable roomId: Long): Response<List<ProjectRoomBlockResponse>> =
        projectRoomUseCase.getBlocks(roomId)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @DeleteMapping("/{roomId}/blocks/{blockId}")
    fun unblock(@PathVariable roomId: Long, @PathVariable blockId: Long): Response<Any> =
        projectRoomUseCase.unblock(roomId, blockId)
}
