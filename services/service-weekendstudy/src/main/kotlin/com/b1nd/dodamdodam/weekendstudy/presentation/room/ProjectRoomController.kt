package com.b1nd.dodamdodam.weekendstudy.presentation.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.room.ProjectRoomUseCase
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.BlockProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
