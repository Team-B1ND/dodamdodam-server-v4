package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface NightStudyBannedRepository: JpaRepository<NightStudyBannedEntity, Long> {
    fun existsByUserId(userId: UUID): Boolean
    fun findByUserId(userId: UUID): NightStudyBannedEntity?
    fun deleteByUserId(userId: UUID)
}