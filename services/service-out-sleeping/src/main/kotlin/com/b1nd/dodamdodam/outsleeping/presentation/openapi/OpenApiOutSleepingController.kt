package com.b1nd.dodamdodam.outsleeping.presentation.openapi

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.outsleeping.application.openapi.OpenApiOutSleepingUseCase
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.OutSleepingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/openapi")
class OpenApiOutSleepingController(
    private val openApiOutSleepingUseCase: OpenApiOutSleepingUseCase
) {
    @GetMapping("/search")
    fun getValid() =
        openApiOutSleepingUseCase.getValid()
}