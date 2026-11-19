package com.b1nd.dodamdodam.weekendstudy.domain.room.service

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.ProjectRoomAlreadyExistsException
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.ProjectRoomNotFoundException
import com.b1nd.dodamdodam.weekendstudy.domain.room.repository.ProjectRoomRepository
import org.springframework.stereotype.Service

@Service
class ProjectRoomService(
    private val projectRoomRepository: ProjectRoomRepository,
) {
    fun save(room: ProjectRoomEntity) {
        checkNameExists(room.name)
        projectRoomRepository.save(room)
    }

    fun getAll(): List<ProjectRoomEntity> = projectRoomRepository.findAll()

    fun getById(id: Long): ProjectRoomEntity =
        projectRoomRepository.findById(id).orElseThrow { ProjectRoomNotFoundException() }

    fun update(id: Long, name: String, floor: Int) {
        val room = getById(id)
        if (room.name != name) checkNameExists(name)
        room.update(name, floor)
    }

    fun delete(id: Long) {
        projectRoomRepository.delete(getById(id))
    }

    private fun checkNameExists(name: String) {
        if (projectRoomRepository.existsByName(name)) throw ProjectRoomAlreadyExistsException()
    }
}
