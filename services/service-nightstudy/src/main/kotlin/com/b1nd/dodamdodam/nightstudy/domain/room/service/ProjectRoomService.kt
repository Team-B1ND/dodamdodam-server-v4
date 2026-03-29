package com.b1nd.dodamdodam.nightstudy.domain.room.service

import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.ProjectRoomAlreadyExistsException
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.ProjectRoomNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.room.repository.ProjectRoomRepository
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

    fun update(id: Long, name: String) {
        checkNameExists(name)
        getById(id).update(name)
    }

    fun delete(id: Long) {
        val room = getById(id)
        projectRoomRepository.delete(room)
    }

    private fun checkNameExists(name: String) {
        if (projectRoomRepository.existsByName(name)) throw ProjectRoomAlreadyExistsException()
    }
}
