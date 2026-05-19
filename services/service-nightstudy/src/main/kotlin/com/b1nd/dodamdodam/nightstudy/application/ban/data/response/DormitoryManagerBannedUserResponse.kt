package com.b1nd.dodamdodam.nightstudy.application.ban.data.response

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DormitoryManagerBannedUserResponse(
    val userId: UUID,
    val reason: String,
    val name: String,
    val student: StudentInfo,
    val phone: String,
    val endAt: LocalDate,
    val createdAt: LocalDateTime,
) {
    data class StudentInfo(
        val grade: Int,
        val room: Int,
        val number: Int,
    )

    companion object {

    }
}
