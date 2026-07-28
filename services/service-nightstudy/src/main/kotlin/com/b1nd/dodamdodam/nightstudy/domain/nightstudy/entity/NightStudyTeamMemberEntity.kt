package com.b1nd.dodamdodam.nightstudy.domain.team

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
import java.util.UUID

@Entity
@Table(name = "night_study_team_member")
class NightStudyTeamMemberEntity(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_project_team_id", nullable = false)
    val team: NightStudyTeamEntity,

    @Column(name = "fk_user_id", nullable = false)
    val user: UUID,

    var isOwner: Boolean = false,

    var isAccept: Boolean = false
): BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun isAccept() {
        this.isAccept = true
    }
}