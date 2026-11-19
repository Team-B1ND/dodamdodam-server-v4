package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WeekendStudyRepository : JpaRepository<WeekendStudyEntity, Long> {
    fun findByPublicId(publicId: UUID): WeekendStudyEntity?
}
