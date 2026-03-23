package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface NightStudyMemberRepository: JpaRepository<NightStudyMemberEntity, Long> {
    fun findAllByUserId(userId: UUID): List<NightStudyMemberEntity>
    fun deleteAllByNightStudy(nightStudy: NightStudyEntity)
    
    @Query("select m.userId from NightStudyMemberEntity m where m.nightStudy = :nightStudy")
    fun findAllUserIdsByNightStudy(nightStudy: NightStudyEntity): List<UUID>

    @Query("select m.userId from NightStudyMemberEntity m where m.nightStudy = :nightStudy and m.isLeader = true")
    fun findLeaderUserIdByNightStudy(nightStudy: NightStudyEntity): UUID?

    fun existsByNightStudyAndUserId(nightStudy: NightStudyEntity, userId: UUID): Boolean
}