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

        if (hasPeriodOverlap(userId, nightStudy.period, nightStudy.type, nightStudy.startAt, nightStudy.endAt)) {
            throw PeriodOverlappedException()
        }

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
        val nightStudy = getByPublicId(publicId)
        val isMine = isMine(userId, nightStudy)

        if (!isMine) throw NotMyNightStudyException()

        if (nightStudy.type == NightStudyType.PROJECT) {
            val leaderId = getLeaderByNightStudy(nightStudy)
            if (leaderId != userId) throw NotLeaderException()
        }
        if (nightStudy.status == NightStudyStatusType.ALLOWED)
            throw AlreadyApprovedException()

        nightStudyMemberRepository.deleteAllByNightStudy(nightStudy)
        nightStudyRepository.delete(nightStudy)
    }

    fun allow(publicId: UUID) {
        val ns = getByPublicId(publicId)
        val wasAlreadyAllowed = ns.status == NightStudyStatusType.ALLOWED
        ns.allow()
        if (ns.type == NightStudyType.PROJECT && !wasAlreadyAllowed) {
            val memberIds = nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(ns)
            memberIds.forEach { memberId ->
                val hasPersonal = nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(
                     memberId, 2, NightStudyType.PERSONAL, ns.startAt, ns.endAt
                )
                if (hasPersonal) return@forEach

                val nightStudy = NightStudyEntity(
                        description = "프로젝트 심야자습으로 인한 자동 신청",
                        period = if (ns.period == 1) 2 else 1,
                        type = NightStudyType.PERSONAL,
                        startAt = ns.startAt,
                        endAt = ns.endAt,
                        status = NightStudyStatusType.ALLOWED,
                        needPhone = false
                )

                save(nightStudy, memberId, null)
            }
        }
    }

    fun reject(publicId: UUID, rejectionReason: String) {
        getByPublicId(publicId).reject(rejectionReason)
    }

    fun pending(publicId: UUID) {
        getByPublicId(publicId).pending()
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

    private fun hasPeriodOverlap(
        userId: UUID,
        period: Int,
        type: NightStudyType,
        startAt: LocalDate,
        endAt: LocalDate
    ): Boolean {
        return nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(userId, period, type, startAt, endAt)
    }
}
