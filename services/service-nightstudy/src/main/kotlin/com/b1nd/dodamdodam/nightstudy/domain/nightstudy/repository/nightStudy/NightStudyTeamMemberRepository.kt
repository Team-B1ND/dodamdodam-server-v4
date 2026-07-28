package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamMemberEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NightStudyTeamMemberRepository: JpaRepository<NightStudyTeamMemberEntity, Long> {
    fun findByUser(userId: UUID, pageable: Pageable): Page<NightStudyTeamMemberEntity>
//    fun findByUser(userId: UUID): NightStudyTeamMemberEntity
    fun findByUserAndTeamAndIsOwnerTrue(userId: UUID, team: NightStudyTeamEntity): NightStudyTeamMemberEntity?
    fun findByTeam(team: NightStudyTeamEntity): List<NightStudyTeamMemberEntity>
    fun findByTeamAndUserIn(team: NightStudyTeamEntity, users: List<UUID>): List<NightStudyTeamMemberEntity>
    fun deleteByTeam(team: NightStudyTeamEntity)
    fun existsByTeamAndUser(team: NightStudyTeamEntity, user: UUID): Boolean
    fun findByUserAndTeam(userId: UUID, team: NightStudyTeamEntity): NightStudyTeamMemberEntity
}
