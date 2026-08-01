package com.b1nd.dodamdodam.nightstudy.presentation.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.room.ProjectRoomUseCase
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomDetailResponse
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomSummaryResponse
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/rooms")
class ProjectRoomController(
    private val projectRoomUseCase: ProjectRoomUseCase,
) {

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PostMapping
    fun create(@RequestBody @Valid request: SaveProjectRoomRequest): Response<Any> =
        projectRoomUseCase.create(request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping
    fun getAll(): Response<List<ProjectRoomResponse>> =
        projectRoomUseCase.getAll()

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/status")
    fun getAllWithStatus(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<List<RoomSummaryResponse>> =
        projectRoomUseCase.getAllWithStatus(date ?: LocalDate.now(), period)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/status/{roomId}")
    fun getByRoomId(
        @PathVariable roomId: String,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<RoomDetailResponse> =
        projectRoomUseCase.getByRoomId(roomId, date ?: LocalDate.now(), period)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: SaveProjectRoomRequest): Response<Any> =
        projectRoomUseCase.update(id, request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Response<Any> =
        projectRoomUseCase.delete(id)
}
