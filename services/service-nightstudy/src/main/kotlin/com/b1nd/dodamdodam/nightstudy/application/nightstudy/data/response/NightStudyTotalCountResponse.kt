package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

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
        val period1: GenderCount,
        val period2: GenderCount,
    )

    data class GenderCount(
        val male: Int,
        val female: Int,
    )

    data class MemberCount(
        val floor: Int,
        val grade: Int,
        val period: Int,
        val gender: Gender,
    )

    enum class Gender {
        MALE,
        FEMALE,
    }

    companion object {
        fun of(members: List<MemberCount>): NightStudyTotalCountResponse {
            fun genderCount(filtered: List<MemberCount>, period: Int) = GenderCount(
                male = filtered.count { it.period == period && it.gender == Gender.MALE },
                female = filtered.count { it.period == period && it.gender == Gender.FEMALE },
            )

            fun periodCount(filtered: List<MemberCount>) = PeriodCount(
                period1 = genderCount(filtered, 1),
                period2 = genderCount(filtered, 2),
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
