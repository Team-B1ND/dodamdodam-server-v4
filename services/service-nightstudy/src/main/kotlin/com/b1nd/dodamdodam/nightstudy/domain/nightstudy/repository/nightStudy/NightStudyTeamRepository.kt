package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NightStudyTeamRepository: JpaRepository<NightStudyTeamEntity, Long> {
    fun findByPublicId(publicId: UUID?): NightStudyTeamEntity?
    override fun findAll(pageable: Pageable): Page<NightStudyTeamEntity>
}