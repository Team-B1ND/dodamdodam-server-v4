package com.b1nd.dodamdodam.weekendstudy.domain.deadline.repository

import com.b1nd.dodamdodam.weekendstudy.domain.deadline.entity.WeekendStudyDeadlineEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WeekendStudyDeadlineRepository : JpaRepository<WeekendStudyDeadlineEntity, Long>
