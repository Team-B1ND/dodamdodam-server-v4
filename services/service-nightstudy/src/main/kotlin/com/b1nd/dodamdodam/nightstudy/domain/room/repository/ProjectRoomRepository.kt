package com.b1nd.dodamdodam.nightstudy.domain.room.repository

import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRoomRepository : JpaRepository<ProjectRoomEntity, Long> {
    fun existsByName(name: String): Boolean
}
