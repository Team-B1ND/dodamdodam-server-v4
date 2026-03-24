package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyBannedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NotLeaderException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NotMyNightStudyException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.NightStudyBannedRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.NightStudyMemberQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.NightStudyMemberRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.NightStudyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
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
        if(isBanned(userId)) throw NightStudyBannedException()

        if(hasPeriodOverlap(userId, nightStudy.startAt, nightStudy.endAt)) {
            throw PeriodOverlappedException()
        }

        members?.forEach { member ->
            if (isBanned(member)) throw NightStudyBannedException()
            if(hasPeriodOverlap(member, nightStudy.startAt, nightStudy.endAt)) {
                throw PeriodOverlappedException()
            }
        }

        val savedNightStudy = nightStudyRepository.save(nightStudy)

        nightStudyMemberRepository.save(NightStudyMemberEntity(savedNightStudy, userId, true))
        members?.forEach { memberId ->
            nightStudyMemberRepository.save(NightStudyMemberEntity(savedNightStudy, memberId))
        }
    }

    fun getAllByUserIdAndStatusAndType(userId: UUID, status: NightStudyStatusType, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity> {
        return nightStudyQueryRepository.findAllByUserIdAndStatusAndType(userId, status, type, pageable)
    }

    fun getAllByType(type: NightStudyType, pageable: Pageable): Page<NightStudyEntity> {
        return nightStudyQueryRepository.findAllByType(type, pageable)
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

    fun getNightStudyWithMembers(nightStudy: NightStudyEntity): NightStudyWithMembersCommand {
        val memberIds = getMembersByNightStudy(nightStudy)
        val leaderId = getLeaderByNightStudy(nightStudy)
        return NightStudyWithMembersCommand(
            nightStudy = nightStudy,
            leaderId = leaderId,
            memberIds = memberIds
        )
    }

    fun delete(userId: UUID, publicId: UUID) {
        val nightStudy = getByPublicId(publicId)
        val isMine = isMine(userId, nightStudy)

        if (!isMine) throw NotMyNightStudyException()

        if (nightStudy.type == NightStudyType.PROJECT) {
            val leaderId = getLeaderByNightStudy(nightStudy)
            if (leaderId != userId) throw NotLeaderException()
        }

        nightStudyMemberRepository.deleteAllByNightStudy(nightStudy)
        nightStudyRepository.delete(nightStudy)
    }

    fun allow(publicId: UUID) {
        getByPublicId(publicId).allow()
    }

    fun reject(publicId: UUID, rejectionReason: String) {
        getByPublicId(publicId).reject(rejectionReason)
    }

    fun pending(publicId: UUID) {
        getByPublicId(publicId).pending()
    }

    private fun isBanned(userId: UUID): Boolean {
        return bannedRepository.existsByUserId(userId)
    }

    private fun isMine(userId: UUID, nightStudy: NightStudyEntity): Boolean {
        return nightStudyMemberRepository.existsByNightStudyAndUserId(nightStudy, userId)
    }

    private fun hasPeriodOverlap(userId: UUID, startAt: java.time.LocalDate, endAt: java.time.LocalDate): Boolean {
        return nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(userId, startAt, endAt)
    }
}