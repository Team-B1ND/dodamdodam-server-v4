package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

data class NightStudyApprovedCountResponse(
    val personal: PeriodCount,
    val project: PeriodCount,
    val total: PeriodCount,
) {
    data class PeriodCount(
        val period1: Long,
        val period2: Long,
    )
}
