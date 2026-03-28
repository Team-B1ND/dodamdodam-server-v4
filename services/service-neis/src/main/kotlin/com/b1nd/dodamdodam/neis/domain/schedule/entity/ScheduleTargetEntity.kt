package com.b1nd.dodamdodam.neis.domain.schedule.entity

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "schedule_targets")
class ScheduleTargetEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_schedule_id", nullable = false)
    val schedule: ScheduleEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val target: ScheduleTarget,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
