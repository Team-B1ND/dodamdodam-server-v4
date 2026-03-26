package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity

import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "night_study_members")
class NightStudyMemberEntity (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_night_study_id")
    var nightStudy: NightStudyEntity,

    @Column(name = "fk_user_id", columnDefinition = "BINARY(16)")
    var userId: UUID,

    var isLeader: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}