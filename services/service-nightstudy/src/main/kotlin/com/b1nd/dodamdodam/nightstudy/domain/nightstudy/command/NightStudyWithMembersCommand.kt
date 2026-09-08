package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.util.UUID

data class NightStudyWithMembersCommand(
    val nightStudy: NightStudyEntity,
    val leaderId: UUID?,
    val memberIds: List<UUID>,
    val participations: Participations,
) {
    data class Participations(
        private val values: Map<UUID, Participation>,
    ) {
        operator fun get(userId: UUID): Participation = values[userId] ?: Participation.NONE

        fun isEmpty(): Boolean = values.isEmpty()

        companion object {
            val EMPTY = Participations(emptyMap())
        }
    }

    data class Participation(
        val attendance: Attendance,
        val assignment: Assignment,
    ) {
        companion object {
            val NONE = Participation(Attendance.NOT_APPLIED, Assignment.NONE)
        }
    }

    data class Attendance(
        val period1: NightStudyAttendanceStatus,
        val period2: NightStudyAttendanceStatus,
    ) {
        companion object {
            val NOT_APPLIED = Attendance(
                period1 = NightStudyAttendanceStatus.NOT_APPLIED,
                period2 = NightStudyAttendanceStatus.NOT_APPLIED,
            )
        }
    }

    data class Assignment(
        val period1: PeriodAssignment?,
        val period2: PeriodAssignment?,
    ) {
        companion object {
            val NONE = Assignment(period1 = null, period2 = null)
        }
    }

    data class PeriodAssignment(
        val type: NightStudyType,
        val projectRoomName: String?,
        val projectRoomFloor: Int?,
    )
}
