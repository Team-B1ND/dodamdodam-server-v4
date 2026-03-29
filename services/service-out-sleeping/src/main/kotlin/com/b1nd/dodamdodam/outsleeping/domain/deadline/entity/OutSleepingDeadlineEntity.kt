package com.b1nd.dodamdodam.outsleeping.domain.deadline.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "out_sleeping_deadlines")
class OutSleepingDeadlineEntity(

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var startDayOfWeek: DayOfWeek,

    @Column(nullable = false)
    var startTime: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var endDayOfWeek: DayOfWeek,

    @Column(nullable = false)
    var endTime: LocalTime,
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun update(startDayOfWeek: DayOfWeek, startTime: LocalTime, endDayOfWeek: DayOfWeek, endTime: LocalTime) {
        this.startDayOfWeek = startDayOfWeek
        this.startTime = startTime
        this.endDayOfWeek = endDayOfWeek
        this.endTime = endTime
    }
}
