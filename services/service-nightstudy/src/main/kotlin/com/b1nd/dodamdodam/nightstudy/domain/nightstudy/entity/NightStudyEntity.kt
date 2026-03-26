package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
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
@Table(name = "night_studies")
class NightStudyEntity (
    @Column(length = 100)
    var name: String? = null,

    @Column(length = 250)
    var description: String,

    var period: Int,

    var startAt: LocalDate,

    var endAt: LocalDate,

    var needPhone: Boolean,

    var needPhoneReason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    var status: NightStudyStatusType = NightStudyStatusType.PENDING,

    var rejectionReason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    var type: NightStudyType,
): BaseTimeEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_room_id")
    var room: ProjectRoomEntity? = null
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    var publicId: UUID? = null
        protected set

    @PrePersist
    fun generatePublicId() {
        publicId = UUID.randomUUID()
    }

    fun allow() {
        this.status = NightStudyStatusType.ALLOWED
    }

    fun reject(rejectionReason: String) {
        this.status = NightStudyStatusType.REJECTED
        this.rejectionReason = rejectionReason
    }

    fun pending() {
        this.status = NightStudyStatusType.PENDING
        this.rejectionReason = null
    }

    fun assignRoom(room: ProjectRoomEntity) {
        this.room = room
    }

    fun unassignRoom() {
        this.room = null
    }
}