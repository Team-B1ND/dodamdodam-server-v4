package com.b1nd.dodamdodam.neis.presentation.meal

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.neis.application.meal.MealUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/meal")
class MealController(
    private val mealUseCase: MealUseCase,
) {
    @UserAccess
    @GetMapping
    fun getMealsByDate(@RequestParam date: LocalDate) =
        mealUseCase.getMealsByDate(date)

    @UserAccess
    @GetMapping("/month")
    fun getMealsByMonth(@RequestParam date: LocalDate) =
        mealUseCase.getMealsByMonth(date)

    @UserAccess(roles = [RoleType.ADMIN])
    @PostMapping("/sync")
    fun syncMeals(@RequestParam date: LocalDate) =
        mealUseCase.syncMeals(date)
}