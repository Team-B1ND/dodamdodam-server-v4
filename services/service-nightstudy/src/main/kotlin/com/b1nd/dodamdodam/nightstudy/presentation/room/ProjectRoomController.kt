package com.b1nd.dodamdodam.nightstudy.presentation.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.room.ProjectRoomUseCase
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

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
    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: SaveProjectRoomRequest): Response<Any> =
        projectRoomUseCase.update(id, request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Response<Any> =
        projectRoomUseCase.delete(id)
}
