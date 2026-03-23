package com.b1nd.dodamdodam.neis.presentation.timetable

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.neis.application.timetable.TimeTableUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/time-table")
class TimeTableController(
    private val timeTableUseCase: TimeTableUseCase,
) {
    @UserAccess
    @GetMapping("/me")
    fun getMyTimeTables() = timeTableUseCase.getMyWeeklyTimeTables()

    @UserAccess
    @GetMapping
    fun getTimeTables(@RequestParam date: LocalDate, @RequestParam grade: Int, @RequestParam room: Int) =
        timeTableUseCase.getTimeTablesByDate(date, grade, room)

    @UserAccess
    @GetMapping("/all")
    fun getAllTimeTables(@RequestParam date: LocalDate) =
        timeTableUseCase.getAllTimeTablesByDate(date)

    @UserAccess(roles = [RoleType.ADMIN])
    @PostMapping("/sync/weekly")
    fun syncWeeklyTimeTables(@RequestParam mondayDate: LocalDate) =
        timeTableUseCase.syncWeeklyTimeTables(mondayDate)

    @UserAccess(roles = [RoleType.ADMIN])
    @PostMapping("/sync/daily")
    fun syncDailyTimeTables(@RequestParam date: LocalDate) =
        timeTableUseCase.syncDailyTimeTables(date)
}
