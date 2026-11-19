package com.b1nd.dodamdodam.weekendstudy.domain.room.service

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomBlockEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.ProjectRoomAlreadyBlockedException
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.ProjectRoomBlockNotFoundException
import com.b1nd.dodamdodam.weekendstudy.domain.room.exception.ProjectRoomBlockedException
import com.b1nd.dodamdodam.weekendstudy.domain.room.repository.ProjectRoomBlockRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProjectRoomBlockService(
    private val projectRoomBlockRepository: ProjectRoomBlockRepository,
) {
    fun saveAll(blocks: List<ProjectRoomBlockEntity>) {
        val first = blocks.first()
        if (projectRoomBlockRepository.existsByRoomAndPeriodInAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                first.room, blocks.map { it.period }, first.endAt, first.startAt))
            throw ProjectRoomAlreadyBlockedException()
        try {
            projectRoomBlockRepository.saveAll(blocks)
        } catch (e: DataIntegrityViolationException) {
            throw ProjectRoomAlreadyBlockedException()
        }
    }

    fun getAllByRoom(room: ProjectRoomEntity): List<ProjectRoomBlockEntity> =
        projectRoomBlockRepository.findAllByRoomOrderByStartAtDescPeriodAsc(room)

    fun delete(room: ProjectRoomEntity, blockId: Long) {
        val block = projectRoomBlockRepository.findByIdAndRoom(blockId, room)
            ?: throw ProjectRoomBlockNotFoundException()
        projectRoomBlockRepository.delete(block)
    }

    fun validateNotBlocked(room: ProjectRoomEntity, date: LocalDate, periods: List<Int>) {
        if (projectRoomBlockRepository.existsByRoomAndPeriodInAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                room, periods, date, date))
            throw ProjectRoomBlockedException()
    }
}
