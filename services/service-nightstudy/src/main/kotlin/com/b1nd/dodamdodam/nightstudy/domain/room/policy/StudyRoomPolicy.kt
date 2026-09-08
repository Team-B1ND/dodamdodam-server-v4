package com.b1nd.dodamdodam.nightstudy.domain.room.policy

object StudyRoomPolicy {
    private const val DEFAULT_FLOOR = 2
    private const val UPPER_FLOOR = 3
    private const val THIRD_GRADE_ROOM_NAME = "GRADE_3"

    fun classRoomName(grade: Int, classNo: Int): String? = when (grade) {
        1, 2 -> "CLASS_${grade}_${classNo}"
        3 -> THIRD_GRADE_ROOM_NAME
        else -> null
    }

    fun classRoomFloor(grade: Int, classNo: Int): Int =
        if (grade == 2 || (grade == 1 && classNo == 4)) UPPER_FLOOR else DEFAULT_FLOOR
}
