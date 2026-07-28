package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.AlreadyJoinedTeamException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyNotTeamOwnerException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyTeamInvitationNotReceivedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyTeamNotFound
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyTeamMemberRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyTeamRepository
import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamMemberEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class NightStudyTeamService(
    private val nightStudyTeamRepository: NightStudyTeamRepository,
    private val nightStudyTeamMemberRepository: NightStudyTeamMemberRepository
) {
    fun createTeam(entity: NightStudyTeamEntity, userid: UUID) {
        val nightStudyTeamEntity = nightStudyTeamRepository.save(entity)
        nightStudyTeamMemberRepository.save(
            NightStudyTeamMemberEntity(
                team = nightStudyTeamEntity,
                user = userid,
                isOwner = true,
                isAccept = true
            ))
    }

    fun inviteMembers(team: NightStudyTeamEntity, userIds: List<UUID>): List<UUID> {
        val requested = userIds.distinct()
        val alreadyJoined = nightStudyTeamMemberRepository.findByTeamAndUserIn(team, requested)
            .map { it.user }
            .toSet()
        val invitees = requested.filterNot { it in alreadyJoined }

        if (invitees.isEmpty()) return emptyList()

        nightStudyTeamMemberRepository.saveAll(
            invitees.map { userId ->
                NightStudyTeamMemberEntity(
                    team = team,
                    user = userId,
                    isOwner = false,
                    isAccept = false
                )
            }
        )

        return invitees
    }

    fun findByUserId(userId: UUID, pageable: Pageable): Page<NightStudyTeamEntity> =
        nightStudyTeamMemberRepository.findByUser(userId, pageable)
            .map { it.team }

    fun findByLeaderId(publicId: UUID?, userId: UUID): NightStudyTeamEntity {
        val team = nightStudyTeamRepository.findByPublicId(publicId)
            ?: throw NightStudyTeamNotFound()

        ensureUserIsTeamOwner(userId, team)

        return team
    }

    fun acceptInvitation(publicId: UUID, userId: UUID): NightStudyTeamMemberEntity {
        val team = nightStudyTeamRepository.findByPublicId(publicId)
            ?: throw NightStudyTeamNotFound()

        val member = isInvite(userId, team)

        member.isAccept()

        return nightStudyTeamMemberRepository.save(member)
    }

    fun rejectInvitation(publicId: UUID, userId: UUID) {
        val team = nightStudyTeamRepository.findByPublicId(publicId)
            ?: throw NightStudyTeamNotFound()

        val member = isInvite(userId, team)

        nightStudyTeamMemberRepository.delete(member)
    }

    fun updateNightStudyTeam(team: NightStudyTeamEntity, newEntity: NightStudyTeamEntity) {
        team.update(
            name = newEntity.name,
            description = newEntity.description,
            imageUrl = newEntity.imageUrl
        )
        nightStudyTeamRepository.save(team)
    }

    fun deleteNightStudyTeam(team: NightStudyTeamEntity) {
        nightStudyTeamMemberRepository.deleteByTeam(team)
        nightStudyTeamRepository.delete(team)
    }

    fun findByTeamMember(teamId: UUID?): List<NightStudyTeamMemberEntity> {
        val team = nightStudyTeamRepository.findByPublicId(teamId)
            ?: throw NightStudyTeamNotFound()

        return nightStudyTeamMemberRepository.findByTeam(team)
    }

    private fun ensureUserIsTeamOwner(userId: UUID, team: NightStudyTeamEntity) {
        nightStudyTeamMemberRepository.findByUserAndTeamAndIsOwnerTrue(userId, team)
            ?: throw NightStudyNotTeamOwnerException()
    }

    private fun isInvite(userId: UUID, team: NightStudyTeamEntity): NightStudyTeamMemberEntity {
        if (!nightStudyTeamMemberRepository.existsByTeamAndUser(team, userId))
            throw NightStudyTeamInvitationNotReceivedException()

        val member = nightStudyTeamMemberRepository.findByUserAndTeam(userId, team)

        if (member.isAccept)
            throw AlreadyJoinedTeamException()

        return member
    }
}