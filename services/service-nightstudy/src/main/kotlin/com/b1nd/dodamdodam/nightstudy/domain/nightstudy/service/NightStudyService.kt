package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyRoomMemberCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.AlreadyApprovedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyBannedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NotLeaderException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NotMyNightStudyException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NotProjectNightStudyException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.RoomAlreadyAssignedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.SourceProjectNotAllowedException
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class NightStudyService(
    private val nightStudyRepository: NightStudyRepository,
    private val nightStudyQueryRepository: NightStudyQueryRepository,
    private val nightStudyMemberRepository: NightStudyMemberRepository,
    private val nightStudyMemberQueryRepository: NightStudyMemberQueryRepository,
    private val bannedRepository: NightStudyBannedRepository
) {
    fun save(nightStudy: NightStudyEntity, userId: UUID, members: List<UUID>?) {
        if (isBanned(userId)) throw NightStudyBannedException()

        members?.forEach { member ->
            if (isBanned(member)) throw NightStudyBannedException()
            if (hasPeriodOverlap(member, nightStudy.period, nightStudy.type, nightStudy.startAt, nightStudy.endAt)) {
                throw PeriodOverlappedException()
            }
        }

        val savedNightStudy = nightStudyRepository.save(nightStudy)

        nightStudyMemberRepository.save(NightStudyMemberEntity(savedNightStudy, userId, true))
        members?.forEach { memberId ->
            nightStudyMemberRepository.save(NightStudyMemberEntity(savedNightStudy, memberId))
        }
    }

    fun getAllByUserIdAndType(userId: UUID, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity> {
        return nightStudyQueryRepository.findAllByUserIdAndType(userId, type, pageable)
    }

    fun getAllByUserIdAndType(userId: UUID, type: NightStudyType): List<NightStudyEntity> {
        return nightStudyQueryRepository.findAllByUserIdAndType(userId, type)
    }

    fun countAllowedMembersGroupByTypeAndPeriod(): Map<Pair<NightStudyType, Int>, Int> {
        return nightStudyQueryRepository.countAllowedMembersGroupByTypeAndPeriod()
    }

    fun getAllowedRoomMembers(date: LocalDate, period: Int): List<NightStudyRoomMemberCommand> {
        return nightStudyQueryRepository.findAllowedRoomMembersByDateAndPeriod(date, period)
    }

    fun searchByType(type: NightStudyType, userIds: List<UUID>?, status: NightStudyStatusType?): List<NightStudyEntity> {
        return if (userIds != null) {
            nightStudyQueryRepository.findAllByTypeAndUserIdsAndStatus(type, userIds, status)
        } else {
            nightStudyQueryRepository.findAllByTypeAndStatus(type, status)
        }
    }

    fun getProjectMemberNightStudyIds(nightStudies: List<NightStudyEntity>): Set<Long> {
        return nightStudyQueryRepository.findProjectMemberNightStudyIds(nightStudies)
    }

    fun getByPublicId(publicId: UUID): NightStudyEntity {
        return nightStudyQueryRepository.findByPublicId(publicId) ?: throw NightStudyNotFoundException()
    }

    fun getMembersByNightStudy(nightStudy: NightStudyEntity): List<UUID> {
        return nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(nightStudy)
    }

    fun getLeaderByNightStudy(nightStudy: NightStudyEntity): UUID? {
        return nightStudyMemberQueryRepository.findLeaderUserIdByNightStudy(nightStudy)
    }

    fun getLeadersByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, UUID> {
        return nightStudyMemberQueryRepository.findLeaderUserIdsByNightStudies(nightStudies)
    }

    fun getMembersByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, List<UUID>> {
        return nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(nightStudies)
    }

    fun delete(userId: UUID, publicId: UUID) {
        val nightStudy = nightStudyRepository.findByPublicIdForUpdate(publicId) ?: throw NightStudyNotFoundException()
        val isMine = isMine(userId, nightStudy)

        if (!isMine) throw NotMyNightStudyException()

        if (nightStudy.type == NightStudyType.PROJECT) {
            val leaderId = getLeaderByNightStudy(nightStudy)
            if (leaderId != userId) throw NotLeaderException()
        }
        if (nightStudy.status == NightStudyStatusType.ALLOWED)
            throw AlreadyApprovedException()

        if (nightStudy.type == NightStudyType.PROJECT) {
            deleteSourceAutos(nightStudy)
        }
        nightStudyMemberRepository.deleteAllByNightStudy(nightStudy)
        nightStudyRepository.delete(nightStudy)
    }

    fun allow(publicId: UUID) {
        val ns = nightStudyRepository.findByPublicIdForUpdate(publicId) ?: throw NightStudyNotFoundException()
        if (
            ns.type == NightStudyType.AUTO &&
            ns.sourceProject?.status?.let { it != NightStudyStatusType.ALLOWED } == true
        ) throw SourceProjectNotAllowedException()

        val wasAlreadyAllowed = ns.status == NightStudyStatusType.ALLOWED
        val memberIds = nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(ns).distinct()

        if (!wasAlreadyAllowed) {
            if (ns.type == NightStudyType.PERSONAL) {
                replaceOverlappingAutosWithPersonal(ns, memberIds)
            }
            memberIds.forEach { memberId ->
                if (hasAllowedPeriodOverlap(memberId, ns.period, ns.startAt, ns.endAt, ns.id!!)) {
                    throw PeriodOverlappedException()
                }
            }
        }

        ns.allow()
        if (ns.type == NightStudyType.PROJECT && !wasAlreadyAllowed) {
            val autoPeriod = if (ns.period == 1) 2 else 1
            val sourceAutosByUserId = getSourceAutosByUserId(ns)
            memberIds.forEach { memberId ->
                val sourceAutos = sourceAutosByUserId[memberId].orEmpty()
                val sourceAutoIds = sourceAutos.mapNotNullTo(mutableSetOf()) { it.id }
                val coveredNightStudies = nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                    userId = memberId,
                    period = autoPeriod,
                    types = AUTO_COVERAGE_TYPES,
                    startAt = ns.startAt,
                    endAt = ns.endAt,
                    excludeNightStudyIds = sourceAutoIds,
                )
                val uncoveredRanges = findUncoveredRanges(
                    startAt = ns.startAt,
                    endAt = ns.endAt,
                    coveredRanges = coveredNightStudies.map { DateRange(it.startAt, it.endAt) },
                )
                syncSourceAutos(ns, memberId, autoPeriod, sourceAutos, uncoveredRanges)
            }
        }
    }

    fun reject(publicId: UUID, rejectionReason: String) {
        val nightStudy = nightStudyRepository.findByPublicIdForUpdate(publicId) ?: throw NightStudyNotFoundException()
        nightStudy.reject(rejectionReason)
        if (nightStudy.type == NightStudyType.PROJECT) {
            nightStudyRepository.findAllBySourceProject(nightStudy).forEach {
                it.reject(SOURCE_PROJECT_REJECTED_REASON)
            }
        }
    }

    fun pending(publicId: UUID) {
        val nightStudy = nightStudyRepository.findByPublicIdForUpdate(publicId) ?: throw NightStudyNotFoundException()
        nightStudy.pending()
        if (nightStudy.type == NightStudyType.PROJECT) {
            nightStudyRepository.findAllBySourceProject(nightStudy).forEach { it.pending() }
        }
    }

    fun assignRoom(publicId: UUID, room: ProjectRoomEntity) {
        val nightStudy = getByPublicId(publicId)
        if (nightStudy.type != NightStudyType.PROJECT) throw NotProjectNightStudyException()
        if (
            nightStudyQueryRepository.existsByRoomAndPeriodOverlap(
                room.id!!,
                nightStudy.period,
                nightStudy.startAt,
                nightStudy.endAt,
                nightStudy.id!!
            )
        ) throw RoomAlreadyAssignedException()
        nightStudy.assignRoom(room)
    }

    fun unassignRoom(publicId: UUID) {
        getByPublicId(publicId).unassignRoom()
    }

    fun getAllAllowed(): List<NightStudyEntity> {
        return nightStudyQueryRepository.findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            NightStudyStatusType.ALLOWED, LocalDate.now(), LocalDate.now()
        )
    }

    private fun isBanned(userId: UUID): Boolean {
        return bannedRepository.existsByUserId(userId)
    }

    private fun isMine(userId: UUID, nightStudy: NightStudyEntity): Boolean {
        return nightStudyMemberRepository.existsByNightStudyAndUserId(nightStudy, userId)
    }

    private fun getSourceAutosByUserId(sourceProject: NightStudyEntity): Map<UUID, List<NightStudyEntity>> {
        val sourceAutos = nightStudyRepository.findAllBySourceProject(sourceProject)
        if (sourceAutos.isEmpty()) return emptyMap()

        val membersByNightStudy = nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(sourceAutos)
        return sourceAutos
            .flatMap { auto -> membersByNightStudy[auto.id].orEmpty().map { userId -> userId to auto } }
            .groupBy({ it.first }, { it.second })
    }

    private fun deleteSourceAutos(sourceProject: NightStudyEntity) {
        nightStudyRepository.findAllBySourceProject(sourceProject).forEach(::deleteAuto)
    }

    private fun syncSourceAutos(
        sourceProject: NightStudyEntity,
        userId: UUID,
        period: Int,
        sourceAutos: List<NightStudyEntity>,
        desiredRanges: List<DateRange>,
    ) {
        val remainingAutos = sourceAutos.toMutableList()

        desiredRanges.forEach { range ->
            val existingAuto = remainingAutos.firstOrNull {
                it.startAt == range.startAt && it.endAt == range.endAt
            }
            if (existingAuto != null) {
                existingAuto.pending()
                remainingAutos.remove(existingAuto)
            } else {
                save(
                    NightStudyEntity(
                        description = sourceProject.description,
                        period = period,
                        type = NightStudyType.AUTO,
                        startAt = range.startAt,
                        endAt = range.endAt,
                        status = NightStudyStatusType.PENDING,
                        needPhone = false,
                        sourceProject = sourceProject,
                    ),
                    userId,
                    null,
                )
            }
        }

        remainingAutos.forEach(::deleteAuto)
    }

    private fun deleteAuto(auto: NightStudyEntity) {
        nightStudyMemberRepository.deleteAllByNightStudy(auto)
        nightStudyRepository.delete(auto)
    }

    private fun replaceOverlappingAutosWithPersonal(
        personal: NightStudyEntity,
        userIds: List<UUID>,
    ) {
        userIds.forEach { userId ->
            val overlappingAutos = nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                userId = userId,
                period = personal.period,
                types = setOf(NightStudyType.AUTO),
                startAt = personal.startAt,
                endAt = personal.endAt,
            )
            overlappingAutos.forEach { auto ->
                val remainingRanges = findUncoveredRanges(
                    startAt = auto.startAt,
                    endAt = auto.endAt,
                    coveredRanges = listOf(DateRange(personal.startAt, personal.endAt)),
                )
                deleteAuto(auto)
                remainingRanges.forEach { range ->
                    save(
                        NightStudyEntity(
                            name = auto.name,
                            description = auto.description,
                            period = auto.period,
                            startAt = range.startAt,
                            endAt = range.endAt,
                            needPhone = auto.needPhone,
                            needPhoneReason = auto.needPhoneReason,
                            status = auto.status,
                            type = NightStudyType.AUTO,
                            sourceProject = auto.sourceProject,
                        ),
                        userId,
                        null,
                    )
                }
            }
        }
    }

    private fun findUncoveredRanges(
        startAt: LocalDate,
        endAt: LocalDate,
        coveredRanges: List<DateRange>,
    ): List<DateRange> {
        if (startAt.isAfter(endAt)) return emptyList()

        val result = mutableListOf<DateRange>()
        var cursor = startAt

        coveredRanges.sortedBy { it.startAt }.forEach { covered ->
            val coveredStart = maxOf(covered.startAt, startAt)
            val coveredEnd = minOf(covered.endAt, endAt)
            if (coveredEnd.isBefore(cursor) || coveredStart.isAfter(endAt)) return@forEach

            if (coveredStart.isAfter(cursor)) {
                result += DateRange(cursor, coveredStart.minusDays(1))
            }

            if (!coveredEnd.isBefore(cursor)) {
                cursor = coveredEnd.plusDays(1)
            }
        }

        if (!cursor.isAfter(endAt)) {
            result += DateRange(cursor, endAt)
        }
        return result
    }

    private fun hasAllowedPeriodOverlap(
        userId: UUID,
        period: Int,
        startAt: LocalDate,
        endAt: LocalDate,
        excludeNightStudyId: Long
    ): Boolean {
        return nightStudyQueryRepository.existsAllowedByUserIdAndPeriodOverlap(
            userId, period, startAt, endAt, excludeNightStudyId
        )
    }

    private fun hasPeriodOverlap(
        userId: UUID,
        period: Int,
        type: NightStudyType,
        startAt: LocalDate,
        endAt: LocalDate
    ): Boolean {
        return nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(userId, period, type, startAt, endAt)
    }

    companion object {
        private const val SOURCE_PROJECT_REJECTED_REASON = "프로젝트 심자 상태 변경으로 인한 자동 거절"
        private val AUTO_COVERAGE_TYPES = setOf(NightStudyType.PERSONAL, NightStudyType.AUTO)
    }

    private data class DateRange(val startAt: LocalDate, val endAt: LocalDate)
}
