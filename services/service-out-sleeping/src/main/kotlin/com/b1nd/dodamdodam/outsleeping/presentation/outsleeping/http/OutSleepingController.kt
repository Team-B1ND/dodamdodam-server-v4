package com.b1nd.dodamdodam.outsleeping.presentation.outsleeping.http

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.OutSleepingUseCase
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ApplyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.DenyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ModifyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.UpdateDeadlineRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/out-sleeping")
class OutSleepingController(
    private val outSleepingUseCase: OutSleepingUseCase,
) {

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("")
    fun apply(@RequestBody request: ApplyOutSleepingRequest) =
        outSleepingUseCase.apply(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @PatchMapping("/{publicId}")
    fun modify(@PathVariable publicId: UUID, @RequestBody request: ModifyOutSleepingRequest) =
        outSleepingUseCase.modify(publicId, request)

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/{publicId}")
    fun cancel(@PathVariable publicId: UUID) =
        outSleepingUseCase.cancel(publicId)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my")
    fun getMy() =
        outSleepingUseCase.getMy()

    @UserAccess(roles = [RoleType.TEACHER, RoleType.DORMITORY_MANAGER])
    @GetMapping("")
    fun getByDate(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate,
        pageable: Pageable,
    ) = outSleepingUseCase.getByDate(date, pageable)

    @UserAccess
    @GetMapping("/valid")
    fun getValid(pageable: Pageable) =
        outSleepingUseCase.getValid(pageable)

    @UserAccess(roles = [RoleType.TEACHER, RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{publicId}/allow")
    fun allow(@PathVariable publicId: UUID) =
        outSleepingUseCase.allow(publicId)

    @UserAccess(roles = [RoleType.TEACHER, RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{publicId}/deny")
    fun deny(@PathVariable publicId: UUID, @RequestBody request: DenyOutSleepingRequest) =
        outSleepingUseCase.deny(publicId, request)

    @UserAccess(roles = [RoleType.TEACHER, RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{publicId}/revert")
    fun revert(@PathVariable publicId: UUID) =
        outSleepingUseCase.revert(publicId)

    @UserAccess
    @GetMapping("/deadline")
    fun getDeadline() =
        outSleepingUseCase.getDeadline()

    @UserAccess(roles = [RoleType.TEACHER, RoleType.DORMITORY_MANAGER])
    @PatchMapping("/deadline")
    fun updateDeadline(@RequestBody request: UpdateDeadlineRequest) =
        outSleepingUseCase.updateDeadline(request)
}
