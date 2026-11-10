package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatus
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingAlreadyProcessedException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingDuplicateDateException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingInvalidDateException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingNotFoundException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingNotOwnerException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserSmsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class OutSleepingService(
    private val outSleepingRepository: OutSleepingRepository,
    private val userQueryClient: UserQueryClient,
    private val userSmsClient: UserSmsClient,
    private val smsScope: CoroutineScope
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun create(outSleeping: OutSleepingEntity): OutSleepingEntity =
        outSleepingRepository.save(outSleeping)

    fun getByPublicId(publicId: UUID): OutSleepingEntity =
        outSleepingRepository.findByPublicId(publicId) ?: throw OutSleepingNotFoundException()

    fun getByUserId(userId: UUID): List<OutSleepingEntity> =
        outSleepingRepository.findAllByUserId(userId)

    fun getByDate(date: LocalDate): List<OutSleepingEntity> =
        outSleepingRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqual(date, date)

    fun getAllowedByDate(date: LocalDate, pageable: Pageable): Page<OutSleepingEntity> =
        outSleepingRepository.findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            OutSleepingStatus.ALLOWED, date, date, pageable
        )

    fun validateOwner(outSleeping: OutSleepingEntity, userId: UUID) {
        if (outSleeping.userId != userId) {
            throw OutSleepingNotOwnerException()
        }
    }

    fun validatePending(outSleeping: OutSleepingEntity) {
        if (outSleeping.status != OutSleepingStatus.PENDING) {
            throw OutSleepingAlreadyProcessedException()
        }
    }

    fun validateDuplicateDate(userId: UUID, startAt: LocalDate, endAt: LocalDate) {
        if (outSleepingRepository.existsByUserIdAndStatusInAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                userId, listOf(OutSleepingStatus.PENDING, OutSleepingStatus.ALLOWED), endAt, startAt
            )) {
            throw OutSleepingDuplicateDateException()
        }
    }

    fun validateDate(startAt: LocalDate, endAt: LocalDate) {
        if (startAt.isBefore(LocalDate.now()) || startAt.isAfter(endAt)) {
            throw OutSleepingInvalidDateException()
        }
    }

    fun delete(outSleeping: OutSleepingEntity) =
        outSleepingRepository.delete(outSleeping)

    fun sendParentNotification(outSleeping: OutSleepingEntity) {
        val outSleepingId = outSleeping.publicId
        val studentId = outSleeping.userId
        val user = runBlocking { userQueryClient.getUser(outSleeping.userId) }
        val phone = runBlocking { userQueryClient.getUser(MALE_DORM_TEACHER_ID) }
        smsScope.launch {
            val accepted = userSmsClient.sendParentSms(studentId, parentNotificationMessage(user.name, outSleeping.startAt, outSleeping.endAt, phone.phone))
            if (accepted) {
                log.info("외박 승인 보호자 문자 발송 완료: outSleepingId={}, studentId={}", outSleepingId, studentId)
            } else {
                log.warn("외박 승인 보호자 문자 미발송: outSleepingId={}, studentId={}", outSleepingId, studentId)
            }
        }
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("M월 d일")
        private val MALE_DORM_TEACHER_ID = UUID.fromString("019d465d-033e-71b3-bb38-8def42e4a6c9")

        private fun parentNotificationMessage(name: String, startAt: LocalDate, endAt: LocalDate, malePhone: String) = """
        [대구소프트웨어마이스터고] 외박 승인 안내
        안녕하십니까 학부모님, ${name} 학생의 외박이 승인되었습니다.

        ◼︎ 외박 기간: ${startAt.format(DATE_FORMAT)} ~ ${endAt.format(DATE_FORMAT)}
        ◼︎ 복귀 시간: ${endAt.format(DATE_FORMAT)} 8시 30분까지

        가족과 행복한 시간 보내시고, 다음날 복귀 시간까지 정상 등교 할 수 있도록 지도 부탁드립니다.
        ※ 문의처
        • 교무실(대표번호): 053-231-9226
          ◦ 남자 사감선생님: ${malePhone}
    """.trimIndent()
    }
}
