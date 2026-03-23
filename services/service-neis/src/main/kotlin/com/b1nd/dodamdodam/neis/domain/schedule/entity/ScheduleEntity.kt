package com.b1nd.dodamdodam.neis.domain.schedule.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "schedules")
class ScheduleEntity(
    @Column(nullable = false, length = 100)
    var title: String,

    @Column(name = "start_at", nullable = false)
    var startAt: LocalDate,

    @Column(name = "end_at", nullable = false)
    var endAt: LocalDate,
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

    fun update(title: String, startAt: LocalDate, endAt: LocalDate) {
        this.title = title
        this.startAt = startAt
        this.endAt = endAt
    }
}
