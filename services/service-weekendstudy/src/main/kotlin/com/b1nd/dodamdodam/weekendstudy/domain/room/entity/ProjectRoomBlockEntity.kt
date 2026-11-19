package com.b1nd.dodamdodam.weekendstudy.domain.room.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
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

@Entity
@Table(
    name = "project_room_blocks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_PROJECT_ROOM_BLOCKS_ON_ROOM_PERIOD_RANGE",
            columnNames = ["fk_room_id", "period", "start_at", "end_at"]
        )
    ]
)
class ProjectRoomBlockEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_room_id", nullable = false)
    var room: ProjectRoomEntity,

    @Column(nullable = false)
    var period: Int,

    @Column(length = 250, nullable = false)
    var description: String,

    @Column(nullable = false)
    var startAt: LocalDate,

    @Column(nullable = false)
    var endAt: LocalDate
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
