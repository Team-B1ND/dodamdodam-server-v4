package com.b1nd.dodamdodam.user.application.user

import com.b1nd.dodamdodam.user.domain.student.exception.ParentPhoneNotFoundException
import com.b1nd.dodamdodam.user.domain.student.service.StudentService
import com.b1nd.dodamdodam.user.domain.user.service.UserService
import com.b1nd.dodamdodam.user.infrastructure.sms.GabiaSmsSender
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Transactional(rollbackOn = [Exception::class])
class UserSmsUseCase(
    private val userService: UserService,
    private val studentService: StudentService,
    private val gabiaSmsSender: GabiaSmsSender
) {
    fun sendParentSms(studentPublicId: UUID, message: String): Boolean {
        val user = userService.get(studentPublicId)
        val student = studentService.get(user)
        val parentPhone = student.parentPhone
            ?: throw ParentPhoneNotFoundException()
        gabiaSmsSender.send(parentPhone, message)
        return true
    }
}
