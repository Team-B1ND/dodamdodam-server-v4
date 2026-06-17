package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.List

interface NightStudyRepository: JpaRepository<NightStudyEntity, Long> {
    fun findAllByStatus(status: NightStudyStatusType): List<NightStudyEntity>
}