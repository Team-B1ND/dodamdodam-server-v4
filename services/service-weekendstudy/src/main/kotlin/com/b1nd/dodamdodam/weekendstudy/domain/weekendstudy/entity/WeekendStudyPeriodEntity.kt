package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "weekend_study_periods",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_WEEKEND_STUDY_PERIODS_ON_USER_DATE_PERIOD",
            columnNames = ["fk_user_id", "date", "period"]
        )
    ]
)
class WeekendStudyPeriodEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_weekend_study_id", nullable = false)
    var weekendStudy: WeekendStudyEntity,

    @Column(name = "fk_user_id", columnDefinition = "BINARY(16)", nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(nullable = false)
    var period: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
