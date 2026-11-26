package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity

import com.b1nd.dodamdodam.core.common.uuid.UuidV7
import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "weekend_studies")
class WeekendStudyEntity(
    @Column(name = "fk_user_id", columnDefinition = "BINARY(16)", nullable = false)
    var userId: UUID,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var type: WeekendStudyType,

    @Column(length = 100)
    var name: String? = null,

    @Column(length = 250)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_room_id")
    var room: ProjectRoomEntity? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    var publicId: UUID? = null
        protected set

    @PrePersist
    fun generatePublicId() {
        publicId = UuidV7.generate()
    }

    fun update(name: String, description: String, room: ProjectRoomEntity) {
        this.name = name
        this.description = description
        this.room = room
    }
}
