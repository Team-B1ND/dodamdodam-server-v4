package com.b1nd.dodamdodam.weekendstudy.application.deadline

import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.request.UpdateDeadlineRequest
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.entity.WeekendStudyDeadlineEntity
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.repository.WeekendStudyDeadlineRepository
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.service.WeekendStudyDeadlineService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.DayOfWeek
import java.time.LocalTime

class WeekendStudyDeadlineUseCaseTest {
    private val deadlineRepository = mock(WeekendStudyDeadlineRepository::class.java)
    private val useCase = WeekendStudyDeadlineUseCase(WeekendStudyDeadlineService(deadlineRepository))

    private val deadline = WeekendStudyDeadlineEntity(DayOfWeek.MONDAY, LocalTime.of(0, 0), DayOfWeek.SUNDAY, LocalTime.of(17, 0))

    @Test
    fun `마감 시간을 조회한다`() {
        `when`(deadlineRepository.findAll()).thenReturn(listOf(deadline))

        val response = useCase.getDeadline()

        assertEquals(DayOfWeek.MONDAY, response.data?.startDayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, response.data?.endDayOfWeek)
        assertEquals(LocalTime.of(17, 0), response.data?.endTime)
    }

    @Test
    fun `마감 행이 없으면 data 없이 조회된다`() {
        `when`(deadlineRepository.findAll()).thenReturn(emptyList())

        assertNull(useCase.getDeadline().data)
    }

    @Test
    fun `마감 행이 있으면 그 행을 수정한다`() {
        `when`(deadlineRepository.findAll()).thenReturn(listOf(deadline))

        useCase.updateDeadline(UpdateDeadlineRequest(DayOfWeek.TUESDAY, LocalTime.of(9, 0), DayOfWeek.SATURDAY, LocalTime.of(21, 0)))

        assertEquals(DayOfWeek.TUESDAY, deadline.startDayOfWeek)
        assertEquals(LocalTime.of(9, 0), deadline.startTime)
        assertEquals(DayOfWeek.SATURDAY, deadline.endDayOfWeek)
        assertEquals(LocalTime.of(21, 0), deadline.endTime)
        verify(deadlineRepository, never()).save(any(WeekendStudyDeadlineEntity::class.java))
    }

    @Test
    fun `마감 행이 없으면 새로 저장한다`() {
        `when`(deadlineRepository.findAll()).thenReturn(emptyList())
        doAnswer { it.getArgument<WeekendStudyDeadlineEntity>(0) }
            .`when`(deadlineRepository).save(any(WeekendStudyDeadlineEntity::class.java))

        useCase.updateDeadline(UpdateDeadlineRequest(DayOfWeek.MONDAY, LocalTime.of(0, 0), DayOfWeek.SUNDAY, LocalTime.of(17, 0)))

        val saved = ArgumentCaptor.forClass(WeekendStudyDeadlineEntity::class.java)
        verify(deadlineRepository).save(saved.capture())
        assertEquals(DayOfWeek.SUNDAY, saved.value.endDayOfWeek)
        assertEquals(LocalTime.of(17, 0), saved.value.endTime)
    }
}
