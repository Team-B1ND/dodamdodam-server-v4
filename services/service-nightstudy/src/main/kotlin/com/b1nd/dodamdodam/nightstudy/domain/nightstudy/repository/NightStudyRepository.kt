package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NightStudyRepository: JpaRepository<NightStudyEntity, Long>