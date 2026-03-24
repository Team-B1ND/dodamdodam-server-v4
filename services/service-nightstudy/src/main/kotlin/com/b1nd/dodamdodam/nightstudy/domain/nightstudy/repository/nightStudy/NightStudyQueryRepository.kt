package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface NightStudyQueryRepository {
    fun findByPublicId(publicId: UUID): NightStudyEntity?
    fun findAllByUserIdAndStatusAndType(userId: UUID, status: NightStudyStatusType, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity>
    fun findAllByType(type: NightStudyType, pageable: Pageable): Page<NightStudyEntity>
    fun existsByPublicIdAndUserId(publicId: UUID, userId: UUID): Boolean
    fun existsByUserIdAndPeriodOverlap(userId: UUID, startAt: LocalDate, endAt: LocalDate): Boolean
}