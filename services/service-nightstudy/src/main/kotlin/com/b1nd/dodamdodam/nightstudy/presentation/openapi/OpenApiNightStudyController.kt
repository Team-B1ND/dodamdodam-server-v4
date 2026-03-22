package com.b1nd.dodamdodam.nightstudy.presentation.openapi

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.nightstudy.application.openapi.OpenApiNightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.request.RejectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/openapi/night-study")
class OpenApiNightStudyController(
    private val openApiNightStudyUseCase: OpenApiNightStudyUseCase,
) {
    @GetMapping
    fun findAllByType(@RequestParam type: NightStudyType): Response<List<OpenApiNightStudyResponse>> =
        openApiNightStudyUseCase.findAllByType(type)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): Response<OpenApiNightStudyResponse> =
        openApiNightStudyUseCase.findById(id)

    @PatchMapping("/{id}/allow")
    fun allow(@PathVariable id: Long): Response<Any> =
        openApiNightStudyUseCase.allow(id)

    @PatchMapping("/{id}/reject")
    fun reject(@PathVariable id: Long, @RequestBody request: RejectNightStudyRequest): Response<Any> =
        openApiNightStudyUseCase.reject(id, request.rejectionReason)

    @PatchMapping("/{id}/pending")
    fun pending(@PathVariable id: Long): Response<Any> =
        openApiNightStudyUseCase.pending(id)
}
