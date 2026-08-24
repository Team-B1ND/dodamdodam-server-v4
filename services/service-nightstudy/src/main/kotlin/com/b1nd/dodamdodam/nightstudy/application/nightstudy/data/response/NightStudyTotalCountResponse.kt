package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType

data class NightStudyTotalCountResponse(
    val floors: List<FloorCount>,
    val grades: List<GradeCount>,
    val total: PeriodCount,
) {
    data class FloorCount(
        val floor: Int,
        val count: PeriodCount,
    )

    data class GradeCount(
        val grade: Int,
        val count: PeriodCount,
    )

    data class PeriodCount(
        val period1: TypeCount,
        val period2: TypeCount,
    )

    data class TypeCount(
        val personal: Int,
        val project: Int,
    )

    data class MemberCount(
        val floor: Int,
        val grade: Int,
        val period: Int,
        val type: NightStudyType,
    )

    companion object {
        fun of(members: List<MemberCount>): NightStudyTotalCountResponse {
            fun typeCount(filtered: List<MemberCount>, period: Int) = TypeCount(
                personal = filtered.count { it.period == period && it.type == NightStudyType.PERSONAL },
                project = filtered.count { it.period == period && it.type == NightStudyType.PROJECT },
            )

            fun periodCount(filtered: List<MemberCount>) = PeriodCount(
                period1 = typeCount(filtered, 1),
                period2 = typeCount(filtered, 2),
            )

            val floors = listOf(2, 3).map { floor ->
                FloorCount(
                    floor = floor,
                    count = periodCount(members.filter { it.floor == floor }),
                )
            }
            val grades = (1..3).map { grade ->
                GradeCount(
                    grade = grade,
                    count = periodCount(members.filter { it.grade == grade }),
                )
            }

            return NightStudyTotalCountResponse(
                floors = floors,
                grades = grades,
                total = periodCount(members),
            )
        }
    }
}
