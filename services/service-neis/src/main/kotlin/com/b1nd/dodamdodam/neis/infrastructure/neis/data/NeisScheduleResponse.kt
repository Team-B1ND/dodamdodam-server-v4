package com.b1nd.dodamdodam.neis.infrastructure.neis.data

import com.fasterxml.jackson.annotation.JsonProperty

data class NeisScheduleApiResponse(
    @JsonProperty("SchoolSchedule")
    val schoolSchedule: List<SchoolScheduleInfo>?,
)

data class SchoolScheduleInfo(
    @JsonProperty("head")
    val head: List<Head>?,
    @JsonProperty("row")
    val row: List<NeisScheduleRow>?,
)

data class NeisScheduleRow(
    @JsonProperty("ATPT_OFCDC_SC_CODE")
    val eduOfficeCode: String,
    @JsonProperty("SD_SCHUL_CODE")
    val schoolCode: String,
    @JsonProperty("AA_YMD")
    val scheduleDate: String,
    @JsonProperty("EVENT_NM")
    val eventName: String,
    @JsonProperty("EVENT_CNTNT")
    val eventContent: String?,
    @JsonProperty("ONE_GRADE_EVENT_YN")
    val oneGradeEventYn: String,
    @JsonProperty("TW_GRADE_EVENT_YN")
    val twGradeEventYn: String,
    @JsonProperty("THREE_GRADE_EVENT_YN")
    val threeGradeEventYn: String,
)
