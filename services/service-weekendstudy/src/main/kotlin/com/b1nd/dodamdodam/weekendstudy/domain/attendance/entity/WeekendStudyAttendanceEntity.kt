package com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "weekend_study_attendances")
class WeekendStudyAttendanceEntity(
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_weekend_study_period_id", nullable = false, unique = true)
    var period: WeekendStudyPeriodEntity,

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    var status: WeekendStudyAttendanceStatus,

    @Column(name = "fk_checker_id", columnDefinition = "BINARY(16)", nullable = false)
    var checkerId: UUID,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun update(status: WeekendStudyAttendanceStatus, checkerId: UUID) {
        this.status = status
        this.checkerId = checkerId
    }
}
