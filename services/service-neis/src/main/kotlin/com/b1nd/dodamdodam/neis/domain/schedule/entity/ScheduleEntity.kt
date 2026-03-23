package com.b1nd.dodamdodam.neis.domain.schedule.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import java.time.LocalDate
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import java.util.UUID

@Entity
@Table(name = "schedules")
class ScheduleEntity(
    @Column(nullable = false, length = 100)
    var title: String,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val type: ScheduleType,

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    val targets: MutableList<ScheduleTargetEntity> = mutableListOf(),
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var publicId: UUID? = null
        protected set

    @PrePersist
    fun generatePublicId() {
        publicId = UUID.randomUUID()
    }

    fun update(title: String, startDate: LocalDate, endDate: LocalDate, targets: List<ScheduleTarget>) {
        this.title = title
        this.startDate = startDate
        this.endDate = endDate
        this.targets.clear()
        targets.forEach { target ->
            this.targets.add(ScheduleTargetEntity(this, target))
        }
    }
}
