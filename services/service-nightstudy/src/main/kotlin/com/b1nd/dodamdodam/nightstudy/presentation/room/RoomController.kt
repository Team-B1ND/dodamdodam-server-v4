package com.b1nd.dodamdodam.nightstudy.presentation.room

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.room.RoomUseCase
import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomUseCase: RoomUseCase,
) {

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PostMapping
    fun create(@RequestBody request: SaveRoomRequest): Response<Any> =
        roomUseCase.create(request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping
    fun getAll(): Response<List<RoomResponse>> =
        roomUseCase.getAll()

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: SaveRoomRequest): Response<Any> =
        roomUseCase.update(id, request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): Response<Any> =
        roomUseCase.delete(id)
}
