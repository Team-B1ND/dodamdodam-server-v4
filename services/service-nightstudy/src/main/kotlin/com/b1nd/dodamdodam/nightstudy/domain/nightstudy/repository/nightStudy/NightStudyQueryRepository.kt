package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyRoomMemberCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.room.command.RoomPeriodCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface NightStudyQueryRepository {
    fun findByPublicId(publicId: UUID): NightStudyEntity?
    fun findAllByUserIdAndType(userId: UUID, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity>
    fun findAllByUserIdAndType(userId: UUID, type: NightStudyType): List<NightStudyEntity>
    fun findAllByTypeAndStatus(type: NightStudyType, status: NightStudyStatusType?): List<NightStudyEntity>
    fun findAllByTypeAndUserIdsAndStatus(type: NightStudyType, userIds: List<UUID>, status: NightStudyStatusType?): List<NightStudyEntity>
    fun countAllowedMembersGroupByTypeAndPeriod(): Map<Pair<NightStudyType, Int>, Int>
    fun existsByPublicIdAndUserId(publicId: UUID, userId: UUID): Boolean
    fun existsByUserIdAndPeriodOverlap(userId: UUID, period: Int, type: NightStudyType, startAt: LocalDate, endAt: LocalDate): Boolean
    fun existsAllowedByUserIdAndPeriodOverlap(userId: UUID, period: Int, startAt: LocalDate, endAt: LocalDate, excludeNightStudyId: Long): Boolean
    fun existsAllowedMemberByUserIdAndDateAndPeriod(userId: UUID, date: LocalDate, period: Int): Boolean
    fun findAllowedUserIdsByDateAndPeriod(date: LocalDate, period: Int): List<UUID>
    fun findAllowedRoomMembersByDateAndPeriod(date: LocalDate, period: Int): List<NightStudyRoomMemberCommand>
    fun countAttendedUserIdsByDateAndPeriod(date: LocalDate, period: Int, userIds: List<UUID>): Long
    fun existsByRoomAndPeriodOverlap(roomId: Long, period: Int, startAt: LocalDate, endAt: LocalDate, excludeNightStudyId: Long): Boolean
    fun findActivePersonalsByUserIdsAndPeriodOverlap(userIds: List<UUID>, period: Int, startAt: LocalDate, endAt: LocalDate): List<NightStudyEntity>
    fun findProjectMemberNightStudyIds(nightStudies: List<NightStudyEntity>): Set<Long>
    fun findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(type: NightStudyType, startAt: LocalDate, endAt: LocalDate): List<NightStudyEntity>
    fun findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(status: NightStudyStatusType, startAt: LocalDate, endAt: LocalDate): List<NightStudyEntity>
    fun findInUseRoomPeriods(date: LocalDate): List<RoomPeriodCommand>
}
