package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import java.util.UUID

interface NightStudyMemberQueryRepository {
    fun findAllUserIdsByNightStudy(nightStudy: NightStudyEntity): List<UUID>
    fun findLeaderUserIdByNightStudy(nightStudy: NightStudyEntity): UUID?
    fun findLeaderUserIdsByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, UUID>
    fun findAllMemberUserIdsByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, List<UUID>>
}