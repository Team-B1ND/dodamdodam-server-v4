package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType

data class NightStudyTotalCountResponse(
    val personal: PeriodCount,
    val project: PeriodCount,
) {
    data class PeriodCount(
        val period1: CategoryCount,
        val period2: CategoryCount,
    )

    data class CategoryCount(
        val grades: List<GradeCount>,
        val floors: List<FloorCount>,
    )

    data class GradeCount(
        val grade: Int,
        val male: Int,
        val female: Int,
    )

    data class FloorCount(
        val floor: Int,
        val male: Int,
        val female: Int,
    )

    data class MemberCount(
        val floor: Int,
        val grade: Int,
        val gender: String,
        val period: Int,
        val type: NightStudyType,
    )

    companion object {
        fun of(members: List<MemberCount>): NightStudyTotalCountResponse {
            fun categoryCount(type: NightStudyType, period: Int): CategoryCount {
                val filtered = members.filter { it.type == type && it.period == period }
                val grades = (1..3).map { grade ->
                    val gradeMembers = filtered.filter { it.grade == grade }
                    GradeCount(
                        grade = grade,
                        male = gradeMembers.count { it.gender == "MALE" },
                        female = gradeMembers.count { it.gender == "FEMALE" },
                    )
                }
                val floors = listOf(2, 3).map { floor ->
                    val floorMembers = filtered.filter { it.floor == floor }
                    FloorCount(
                        floor = floor,
                        male = floorMembers.count { it.gender == "MALE" },
                        female = floorMembers.count { it.gender == "FEMALE" },
                    )
                }

                return CategoryCount(grades = grades, floors = floors)
            }

            fun periodCount(type: NightStudyType) = PeriodCount(
                period1 = categoryCount(type, 1),
                period2 = categoryCount(type, 2),
            )

            return NightStudyTotalCountResponse(
                personal = periodCount(NightStudyType.PERSONAL),
                project = periodCount(NightStudyType.PROJECT),
            )
        }
    }
}
