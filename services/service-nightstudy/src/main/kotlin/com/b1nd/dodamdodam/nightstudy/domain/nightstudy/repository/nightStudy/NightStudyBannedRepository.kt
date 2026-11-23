package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

interface NightStudyBannedRepository: JpaRepository<NightStudyBannedEntity, Long> {
    fun existsByUserIdAndEndAtGreaterThanEqual(userId: UUID, endAt: LocalDate): Boolean
    fun findByUserIdAndEndAtGreaterThanEqual(userId: UUID, endAt: LocalDate): NightStudyBannedEntity?
    fun findAllByEndAtGreaterThanEqual(endAt: LocalDate): List<NightStudyBannedEntity>
    fun deleteByUserId(userId: UUID)
}