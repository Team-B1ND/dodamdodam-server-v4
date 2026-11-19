package com.b1nd.dodamdodam.weekendstudy.domain.room.repository

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomBlockEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ProjectRoomBlockRepository : JpaRepository<ProjectRoomBlockEntity, Long> {
    fun existsByRoomAndPeriodInAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
        room: ProjectRoomEntity,
        periods: Collection<Int>,
        endAt: LocalDate,
        startAt: LocalDate,
    ): Boolean
}
