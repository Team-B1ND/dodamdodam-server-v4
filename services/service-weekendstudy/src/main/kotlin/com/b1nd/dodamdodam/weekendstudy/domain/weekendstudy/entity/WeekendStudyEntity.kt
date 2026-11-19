package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity

import com.b1nd.dodamdodam.core.common.uuid.UuidV7
import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyStatusType
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

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    var status: WeekendStudyStatusType = WeekendStudyStatusType.PENDING,

    @Column(length = 100)
    var name: String? = null,

    @Column(length = 250)
    var description: String? = null,

    var rejectionReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_wish_room_id")
    var wishRoom: ProjectRoomEntity? = null,
) : BaseTimeEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_room_id")
    var room: ProjectRoomEntity? = null
        protected set

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

    fun allow() {
        this.status = WeekendStudyStatusType.ALLOWED
    }

    fun reject(rejectionReason: String) {
        this.status = WeekendStudyStatusType.REJECTED
        this.rejectionReason = rejectionReason
    }

    fun pending() {
        this.status = WeekendStudyStatusType.PENDING
        this.rejectionReason = null
        this.room = null
    }

    fun assignRoom(room: ProjectRoomEntity) {
        this.room = room
    }

    fun unassignRoom() {
        this.room = null
    }
}
