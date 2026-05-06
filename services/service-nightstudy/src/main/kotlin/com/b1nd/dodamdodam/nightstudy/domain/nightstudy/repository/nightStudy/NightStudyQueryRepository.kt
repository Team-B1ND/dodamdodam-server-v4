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
    fun findAllByUserIdAndType(userId: UUID, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity>
    fun findAllByUserIdAndType(userId: UUID, type: NightStudyType): List<NightStudyEntity>
    fun findAllByTypeAndStatus(type: NightStudyType, status: NightStudyStatusType?, pageable: Pageable): Page<NightStudyEntity>
    fun findAllByTypeAndUserIdsAndStatus(type: NightStudyType, userIds: List<UUID>, status: NightStudyStatusType?, pageable: Pageable): Page<NightStudyEntity>
    fun existsByPublicIdAndUserId(publicId: UUID, userId: UUID): Boolean
    fun existsByUserIdAndPeriodOverlap(userId: UUID, period: Int, startAt: LocalDate, endAt: LocalDate): Boolean
    fun existsByRoomAndPeriodOverlap(roomId: Long, period: Int, startAt: LocalDate, endAt: LocalDate, excludeNightStudyId: Long): Boolean
}