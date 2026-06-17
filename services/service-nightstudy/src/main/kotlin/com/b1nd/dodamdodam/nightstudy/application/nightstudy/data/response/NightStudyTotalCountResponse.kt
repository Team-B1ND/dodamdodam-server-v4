package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType

data class NightStudyTotalCountResponse(
    val floors: List<FloorCount>,
    val total: PeriodCount,
) {
    data class FloorCount(
        val floor: Int,
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

    companion object {
        fun of(count: Map<Triple<Int, NightStudyType, Int>, Int>): NightStudyTotalCountResponse {
            fun typeCount(floor: Int, period: Int) = TypeCount(
                personal = count[Triple(floor, NightStudyType.PERSONAL, period)] ?: 0,
                project = count[Triple(floor, NightStudyType.PROJECT, period)] ?: 0,
            )
            fun periodCount(floor: Int) = PeriodCount(
                period1 = typeCount(floor,1),
                period2 = typeCount(floor,2),
            )

            val floors = listOf(2, 3).map { floor ->
                FloorCount(floor = floor, count = periodCount(floor))
            }
            val total = PeriodCount(
                period1 = TypeCount(
                    personal = floors.sumOf { it.count.period1.personal },
                    project = floors.sumOf { it.count.period1.project },
                ),
                period2 = TypeCount(
                    personal = floors.sumOf { it.count.period2.personal },
                    project = floors.sumOf { it.count.period2.project },
                ),
            )

            return NightStudyTotalCountResponse(floors = floors, total = total)
        }
    }
}