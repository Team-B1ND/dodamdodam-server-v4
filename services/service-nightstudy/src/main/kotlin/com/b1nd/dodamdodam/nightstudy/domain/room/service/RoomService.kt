package com.b1nd.dodamdodam.nightstudy.domain.room.service

import com.b1nd.dodamdodam.nightstudy.domain.room.entity.RoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.RoomAlreadyExistsException
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.RoomNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.room.repository.RoomRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RoomService(
    private val roomRepository: RoomRepository,
) {

    fun save(room: RoomEntity) {
        checkNameExists(room.name)
        roomRepository.save(room)
    }

    fun getAll(): List<RoomEntity> = roomRepository.findAll()

    fun getByPublicId(publicId: UUID): RoomEntity =
        roomRepository.findByPublicId(publicId) ?: throw RoomNotFoundException()

    fun update(publicId: UUID, name: String) {
        checkNameExists(name)
        getByPublicId(publicId).update(name)
    }

    fun delete(publicId: UUID) {
        val room = getByPublicId(publicId)
        roomRepository.delete(room)
    }

    private fun checkNameExists(name: String) {
        if (roomRepository.existsByName(name)) throw RoomAlreadyExistsException()
    }
}
