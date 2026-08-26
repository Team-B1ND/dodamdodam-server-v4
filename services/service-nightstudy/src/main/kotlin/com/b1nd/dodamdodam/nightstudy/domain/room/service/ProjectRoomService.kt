package com.b1nd.dodamdodam.nightstudy.domain.room.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.room.command.RoomPeriodCommand
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.ProjectRoomAlreadyExistsException
import com.b1nd.dodamdodam.nightstudy.domain.room.exception.ProjectRoomNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.room.repository.ProjectRoomRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProjectRoomService(
    private val nightStudyQueryRepository: NightStudyQueryRepository,
    private val projectRoomRepository: ProjectRoomRepository,
) {

    fun save(room: ProjectRoomEntity) {
        checkNameExists(room.name)
        projectRoomRepository.save(room)
    }

    fun getAll(): List<ProjectRoomEntity> = projectRoomRepository.findAll()

    fun getInUsePeriods(): List<RoomPeriodCommand> =
        nightStudyQueryRepository.findInUseRoomPeriods(LocalDate.now())

    fun getById(id: Long): ProjectRoomEntity =
        projectRoomRepository.findById(id).orElseThrow { ProjectRoomNotFoundException() }

    fun update(id: Long, name: String, floor: Int) {
        checkNameExists(name)
        getById(id).update(name, floor)
    }

    fun delete(id: Long) {
        val room = getById(id)
        projectRoomRepository.delete(room)
    }

    private fun checkNameExists(name: String) {
        if (projectRoomRepository.existsByName(name)) throw ProjectRoomAlreadyExistsException()
    }
}