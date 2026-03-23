package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NightStudyMemberRepository: JpaRepository<NightStudyMemberEntity, Long> {
    fun deleteAllByNightStudy(nightStudy: NightStudyEntity)
    fun existsByNightStudyAndUserId(nightStudy: NightStudyEntity, userId: UUID): Boolean
}