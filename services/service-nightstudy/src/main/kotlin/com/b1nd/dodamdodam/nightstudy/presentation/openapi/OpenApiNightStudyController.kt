package com.b1nd.dodamdodam.nightstudy.presentation.openapi

import com.b1nd.dodamdodam.nightstudy.application.openapi.OpenApiNightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/openapi")
class OpenApiNightStudyController(
    private val openApiNightStudyUseCase: OpenApiNightStudyUseCase
) {

    @GetMapping("/search")
    fun getAttendances(@RequestParam type: NightStudyType, @RequestParam date: LocalDate) =
        openApiNightStudyUseCase.getAttendances(type, date)
}