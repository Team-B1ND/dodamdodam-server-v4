package com.b1nd.dodamdodam.nightstudy.domain.room.repository

import com.b1nd.dodamdodam.nightstudy.domain.room.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoomRepository : JpaRepository<RoomEntity, Long> {
    fun existsByName(name: String): Boolean
    fun findByPublicId(publicId: UUID): RoomEntity?
}