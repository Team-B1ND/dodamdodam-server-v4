package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus
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
        val projectRoom: ProjectRoom,
    ) {
        companion object {
            val NONE = Participation(Attendance.NOT_APPLIED, ProjectRoom.NONE)
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

    data class ProjectRoom(
        val period1: String?,
        val period2: String?,
    ) {
        companion object {
            val NONE = ProjectRoom(period1 = null, period2 = null)
        }
    }
}
