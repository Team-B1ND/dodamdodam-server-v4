package com.b1nd.dodamdodam.user.application.user

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.user.domain.student.exception.ParentPhoneNotFoundException
import com.b1nd.dodamdodam.user.domain.student.service.StudentService
import com.b1nd.dodamdodam.user.domain.user.service.UserService
import com.b1nd.dodamdodam.user.infrastructure.sms.GabiaSmsSender
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import java.util.UUID

@Component
@Transactional(rollbackOn = [Exception::class])
class UserSmsUseCase(
    private val userService: UserService,
    private val studentService: StudentService,
    private val gabiaSmsSender: GabiaSmsSender
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendParentSms(studentPublicId: UUID, message: String): Boolean {
        val parentPhone = try {
            getParentPhone(studentPublicId)
        } catch (ex: BasicException) {
            log.warn("보호자 문자 발송 생략: studentId={}, reason={}", studentPublicId, ex.exceptionCode.code)
            return false
        }

        val startedAt = System.currentTimeMillis()
        return try {
            gabiaSmsSender.send(parentPhone, message)
            log.info(
                "보호자 문자 발송 성공: studentId={}, phone={}, messageLength={}, elapsedMs={}",
                studentPublicId, maskPhone(parentPhone), message.length, System.currentTimeMillis() - startedAt
            )
            true
        } catch (ex: RestClientResponseException) {
            log.error(
                "보호자 문자 발송 실패(가비아 응답 오류): studentId={}, phone={}, status={}, body={}, elapsedMs={}",
                studentPublicId, maskPhone(parentPhone), ex.statusCode.value(), ex.responseBodyAsString,
                System.currentTimeMillis() - startedAt, ex
            )
            false
        } catch (ex: Exception) {
            log.error(
                "보호자 문자 발송 실패: studentId={}, phone={}, elapsedMs={}",
                studentPublicId, maskPhone(parentPhone), System.currentTimeMillis() - startedAt, ex
            )
            false
        }
    }

    private fun getParentPhone(studentPublicId: UUID): String {
        val user = userService.get(studentPublicId)
        val student = studentService.get(user)
        return student.parentPhone
            ?: throw ParentPhoneNotFoundException()
    }

    private fun maskPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 8) return "****"
        return "${digits.take(3)}-****-${digits.takeLast(4)}"
    }
}
