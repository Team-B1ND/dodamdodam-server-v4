package com.b1nd.dodamdodam.nightstudy.domain.deadline.repository

import com.b1nd.dodamdodam.nightstudy.domain.deadline.entity.NightStudyDeadlineEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NightStudyDeadlineRepository : JpaRepository<NightStudyDeadlineEntity, Long>
