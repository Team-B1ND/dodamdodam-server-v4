package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.AlreadyBannedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.BanNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NightStudyBannedService(
    private val bannedRepository: NightStudyBannedRepository,
) {

    fun ban(banned: NightStudyBannedEntity) {
        if (bannedRepository.existsByUserId(banned.userId)) throw AlreadyBannedException()
        bannedRepository.save(banned)
    }

    fun getAll(): List<NightStudyBannedEntity> = bannedRepository.findAll()

    fun findByUserId(userId: UUID): NightStudyBannedEntity? =
        bannedRepository.findByUserId(userId)

    fun unban(userId: UUID) {
        if (!bannedRepository.existsByUserId(userId)) throw BanNotFoundException()
        bannedRepository.deleteByUserId(userId)
    }
}
