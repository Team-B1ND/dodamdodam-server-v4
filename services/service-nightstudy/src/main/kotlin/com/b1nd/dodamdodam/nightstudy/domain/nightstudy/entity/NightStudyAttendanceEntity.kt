package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "night_study_attendances",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_NIGHT_STUDY_ATTENDANCES_ON_USER_DATE_PERIOD",
            columnNames = ["fk_user_id", "date", "period"]
        )
    ]
)
class NightStudyAttendanceEntity(
    @Column(name = "fk_user_id", columnDefinition = "BINARY(16)", nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(nullable = false)
    var period: Int,

    @Column(nullable = false)
    var attended: Boolean,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun update(attended: Boolean) {
        this.attended = attended
    }
}
