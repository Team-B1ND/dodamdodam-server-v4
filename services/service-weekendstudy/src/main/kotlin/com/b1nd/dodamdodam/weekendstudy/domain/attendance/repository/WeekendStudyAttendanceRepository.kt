package com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.WeekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WeekendStudyAttendanceRepository : JpaRepository<WeekendStudyAttendanceEntity, Long> {
    fun findByPeriod(period: WeekendStudyPeriodEntity): WeekendStudyAttendanceEntity?
}
