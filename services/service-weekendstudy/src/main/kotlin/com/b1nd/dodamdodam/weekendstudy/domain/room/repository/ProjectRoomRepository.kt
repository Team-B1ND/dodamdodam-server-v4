package com.b1nd.dodamdodam.weekendstudy.domain.room.repository

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ProjectRoomRepository : JpaRepository<ProjectRoomEntity, Long> {
    fun existsByName(name: String): Boolean
    fun findAllByEnabledTrue(): List<ProjectRoomEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: Long): ProjectRoomEntity?
}
