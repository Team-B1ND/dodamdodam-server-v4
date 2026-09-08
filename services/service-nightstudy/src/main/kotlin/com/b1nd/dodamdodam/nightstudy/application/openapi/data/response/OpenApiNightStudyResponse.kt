package com.b1nd.dodamdodam.nightstudy.application.openapi.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.time.LocalDate

data class OpenApiNightStudyResponse(
    val name: String?,
    val description: String,
    val period: Int,
    val type: NightStudyType,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val status: NightStudyStatusType,
    val leader: Student,
    val members: List<Student>,
) {
    data class Student(
        val name: String,
        val grade: Int,
        val room: Int,
        val number: Int,
        val attended: Attendance,
        val studyRoom: StudyRoom,
    )

    data class Attendance(
        val period1: NightStudyAttendanceStatus,
        val period2: NightStudyAttendanceStatus,
    )

    data class StudyRoom(
        val period1: Room?,
        val period2: Room?,
    )

    data class Room(
        val name: String,
        val floor: Int,
    )
}
