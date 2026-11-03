package com.b1nd.dodamdodam.user.presentation.user.grpc

import com.b1nd.dodamdodam.core.common.coroutine.CoroutineBlockingExecutor
import com.b1nd.dodamdodam.grpc.user.SendParentSmsRequest
import com.b1nd.dodamdodam.user.application.user.UserSmsUseCase
import com.b1nd.dodamdodam.user.domain.student.exception.ParentPhoneNotFoundException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.UUID

class UserSmsGrpcControllerTest {
    private val useCase = mock(UserSmsUseCase::class.java)
    private val controller = UserSmsGrpcController(
        useCase = useCase,
        blockingExecutor = CoroutineBlockingExecutor(Dispatchers.Unconfined),
    )

    private val studentId: UUID = UUID.randomUUID()
    private val request = SendParentSmsRequest.newBuilder()
        .setStudentPublicId(studentId.toString())
        .setMessage("외박이 승인되었어요.")
        .build()

    @Test
    fun `발송에 성공하면 accepted를 반환한다`() {
        `when`(useCase.sendParentSms(studentId, "외박이 승인되었어요.")).thenReturn(true)

        val response = runBlocking { controller.sendParentSms(request) }

        assertEquals(true, response.accepted)
    }

    @Test
    fun `도메인 예외는 NOT_FOUND로 변환한다`() {
        `when`(useCase.sendParentSms(studentId, "외박이 승인되었어요."))
            .thenThrow(ParentPhoneNotFoundException())

        val ex = assertThrows(StatusRuntimeException::class.java) {
            runBlocking { controller.sendParentSms(request) }
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    @Test
    fun `그 외 예외는 INTERNAL로 변환한다`() {
        `when`(useCase.sendParentSms(studentId, "외박이 승인되었어요."))
            .thenThrow(IllegalStateException("가비아 호출 실패"))

        val ex = assertThrows(StatusRuntimeException::class.java) {
            runBlocking { controller.sendParentSms(request) }
        }

        assertEquals(Status.Code.INTERNAL, ex.status.code)
    }
}
