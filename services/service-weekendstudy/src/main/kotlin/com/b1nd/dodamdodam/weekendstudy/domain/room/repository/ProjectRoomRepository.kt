package com.b1nd.dodamdodam.weekendstudy.domain.room.repository

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRoomRepository : JpaRepository<ProjectRoomEntity, Long> {
    fun existsByName(name: String): Boolean
}
