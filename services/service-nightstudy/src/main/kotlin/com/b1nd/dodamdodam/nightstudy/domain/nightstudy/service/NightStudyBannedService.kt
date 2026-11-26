package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.AlreadyBannedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.BanNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class NightStudyBannedService(
    private val bannedRepository: NightStudyBannedRepository,
) {

    fun ban(banned: NightStudyBannedEntity) {
        val today = LocalDate.now()
        if (bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(banned.userId, today)) throw AlreadyBannedException()
        bannedRepository.deleteByUserId(banned.userId)
        bannedRepository.save(banned)
    }

    fun getAll(): List<NightStudyBannedEntity> =
        bannedRepository.findAllByEndAtGreaterThanEqual(LocalDate.now())

    fun findByUserId(userId: UUID): NightStudyBannedEntity? =
        bannedRepository.findByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())

    fun unban(userId: UUID) {
        val today = LocalDate.now()
        if (!bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, today)) throw BanNotFoundException()
        bannedRepository.deleteByUserId(userId)
    }
}
