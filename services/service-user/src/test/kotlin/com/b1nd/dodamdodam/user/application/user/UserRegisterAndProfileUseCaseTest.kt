package com.b1nd.dodamdodam.user.application.user

import com.b1nd.dodamdodam.core.kafka.constants.KafkaTopics
import com.b1nd.dodamdodam.core.kafka.event.user.UserCreatedEvent
import com.b1nd.dodamdodam.core.kafka.event.user.UserUpdatedEvent
import com.b1nd.dodamdodam.core.kafka.producer.KafkaMessageProducer
import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.application.user.data.request.StudentRegisterRequest
import com.b1nd.dodamdodam.user.application.user.data.request.TeacherRegisterRequest
import com.b1nd.dodamdodam.user.application.user.data.request.UpdateUserInfoRequest
import com.b1nd.dodamdodam.user.domain.student.entity.StudentEntity
import com.b1nd.dodamdodam.user.domain.student.service.StudentService
import com.b1nd.dodamdodam.user.domain.teacher.entity.TeacherEntity
import com.b1nd.dodamdodam.user.domain.teacher.service.TeacherService
import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import com.b1nd.dodamdodam.user.domain.user.exception.PhoneAlreadyExistsException
import com.b1nd.dodamdodam.user.domain.user.service.UserQueryService
import com.b1nd.dodamdodam.user.domain.user.service.UserService
import com.b1nd.dodamdodam.user.infrastructure.phoneverification.exception.PhoneNotVerifiedException
import com.b1nd.dodamdodam.user.infrastructure.phoneverification.service.PhoneVerificationStore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

class UserRegisterAndProfileUseCaseTest {
    private val userService = mock(UserService::class.java)
    private val userQueryService = mock(UserQueryService::class.java)
    private val studentService = mock(StudentService::class.java)
    private val teacherService = mock(TeacherService::class.java)
    private val phoneVerificationStore = mock(PhoneVerificationStore::class.java)
    private val kafkaMessageProducer = mock(KafkaMessageProducer::class.java)
    private val useCase = UserUseCase(
        userService = userService,
        userQueryService = userQueryService,
        studentService = studentService,
        teacherService = teacherService,
        phoneVerificationStore = phoneVerificationStore,
        kafkaMessageProducer = kafkaMessageProducer,
    )

    private val userId: UUID = UUID.randomUUID()

    @BeforeEach
    fun login() {
        val passport = Passport(
            userId = userId,
            username = "dodam",
            role = listOf(RoleType.STUDENT),
            enabled = true,
            os = "test",
            version = "1.0.0",
            issuedAt = 0,
            expiredAt = 0,
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(
            PassportUserDetails(passport),
            "",
            emptyList(),
        )
        SecurityContextHolder.setContext(context)
    }

    @AfterEach
    fun logout() {
        SecurityContextHolder.clearContext()
    }

    // ---------- 회원가입 ----------

    @Test
    fun `학생 회원가입 시 성별이 담긴 유저와 학생 정보가 생성된다`() {
        stubCreate()

        val response = useCase.registerStudent(studentRequest(Gender.FEMALE))

        val created = createdUsers().single()
        assertEquals(Gender.FEMALE, created.gender)
        assertEquals("dodam", created.username)
        assertEquals(StatusType.PENDING, created.status)
        assertEquals(RoleType.STUDENT, createdRoles().single())

        val student = createdStudents().single()
        assertEquals(2, student.grade)
        assertEquals(3, student.room)
        assertEquals(11, student.number)

        assertEquals(201, response.status)
        assertEquals("학생 계정이 생성되었어요.", response.message)
    }

    @Test
    fun `학생 회원가입 시 유저 생성 이벤트가 발행된다`() {
        stubCreate()

        useCase.registerStudent(studentRequest(Gender.MALE))

        val event = sentEvents(KafkaTopics.USER_CREATED).single() as UserCreatedEvent
        assertEquals("dodam", event.username)
        assertEquals("박준석", event.name)
        assertEquals("01012345678", event.phone)
        assertEquals(RoleType.STUDENT.name, event.role)
        assertEquals(false, event.status)
    }

    @Test
    fun `회원가입은 전화번호 인증을 먼저 확인한다`() {
        stubCreate()

        useCase.registerStudent(studentRequest(Gender.MALE))

        verify(phoneVerificationStore).ensureActive("01012345678")
    }

    @Test
    fun `전화번호 인증이 되어 있지 않으면 학생 회원가입에 실패한다`() {
        doThrow(PhoneNotVerifiedException())
            .`when`(phoneVerificationStore).ensureActive("01012345678")

        assertThrows(PhoneNotVerifiedException::class.java) {
            useCase.registerStudent(studentRequest(Gender.FEMALE))
        }

        assertEquals(emptyList<UserEntity>(), createdUsers())
        assertEquals(emptyList<StudentEntity>(), createdStudents())
        assertEquals(emptyList<Any>(), sentEvents(KafkaTopics.USER_CREATED))
    }

    @Test
    fun `선생님 회원가입 시 성별이 담긴 유저와 선생님 정보가 생성된다`() {
        stubCreate()

        val response = useCase.registerTeacher(teacherRequest(Gender.MALE))

        val created = createdUsers().single()
        assertEquals(Gender.MALE, created.gender)
        assertEquals(RoleType.TEACHER, createdRoles().single())
        assertEquals("담임", createdTeachers().single().position)

        val event = sentEvents(KafkaTopics.USER_CREATED).single() as UserCreatedEvent
        assertEquals(RoleType.TEACHER.name, event.role)
        assertEquals(201, response.status)
        assertEquals("선생님 계정이 생성되었어요.", response.message)
    }

    @Test
    fun `전화번호 인증이 되어 있지 않으면 선생님 회원가입에 실패한다`() {
        doThrow(PhoneNotVerifiedException())
            .`when`(phoneVerificationStore).ensureActive("01087654321")

        assertThrows(PhoneNotVerifiedException::class.java) {
            useCase.registerTeacher(teacherRequest(Gender.MALE))
        }

        assertEquals(emptyList<UserEntity>(), createdUsers())
        assertEquals(emptyList<TeacherEntity>(), createdTeachers())
    }

    @Test
    fun `모든 성별로 학생 회원가입을 할 수 있다`() {
        Gender.entries.forEach { gender ->
            val userService = mock(UserService::class.java)
            val useCase = UserUseCase(
                userService = userService,
                userQueryService = userQueryService,
                studentService = studentService,
                teacherService = teacherService,
                phoneVerificationStore = phoneVerificationStore,
                kafkaMessageProducer = kafkaMessageProducer,
            )
            `when`(userService.create(any(), any()))
                .thenAnswer { it.getArgument<UserEntity>(0).apply { generatePublicId() } }

            useCase.registerStudent(studentRequest(gender))

            assertEquals(gender, createdUsers(userService).single().gender)
        }
    }

    // ---------- 내 정보 조회 ----------

    @Test
    fun `내 정보를 조회하면 성별과 학생 정보가 함께 반환된다`() {
        val user = user(Gender.FEMALE).apply { generatePublicId() }
        setCreatedAt(user)
        `when`(userService.get(userId)).thenReturn(user)
        `when`(userService.getRoles(user)).thenReturn(setOf(RoleType.STUDENT))
        `when`(studentService.getOrNull(user)).thenReturn(StudentEntity(user, 2, 3, 11))

        val response = useCase.getMyInfo()

        val data = response.data!!
        assertEquals("dodam", data.username)
        assertEquals("박준석", data.name)
        assertEquals(Gender.FEMALE, data.gender)
        assertEquals(setOf(RoleType.STUDENT), data.roles)
        assertEquals(2, data.student!!.grade)
        assertNull(data.teacher)
        assertEquals(200, response.status)
    }

    @Test
    fun `프로필 수정 후 조회하면 변경된 성별이 보인다`() {
        val user = user(Gender.MALE).apply { generatePublicId() }
        setCreatedAt(user)
        `when`(userService.update(any(), any(), any(), any(), any()))
            .thenAnswer { user.apply { updateInfo(null, null, null, Gender.FEMALE) } }
        `when`(userService.get(userId)).thenReturn(user)

        useCase.updateUser(UpdateUserInfoRequest(name = null, phone = null, profileImage = null, gender = Gender.FEMALE))
        val response = useCase.getMyInfo()

        assertEquals(Gender.FEMALE, response.data!!.gender)
    }

    // ---------- 프로필 수정 ----------

    @Test
    fun `프로필 수정 시 성별이 함께 전달된다`() {
        val updated = user(Gender.FEMALE).apply { generatePublicId() }
        `when`(userService.update(any(), any(), any(), any(), any())).thenReturn(updated)

        val response = useCase.updateUser(
            UpdateUserInfoRequest(
                name = "김도담",
                phone = "01099998888",
                profileImage = "https://image.b1nd.com/new.png",
                gender = Gender.FEMALE,
            )
        )

        verify(userService).update(
            userId,
            "김도담",
            "01099998888",
            "https://image.b1nd.com/new.png",
            Gender.FEMALE,
        )
        assertEquals(200, response.status)
        assertEquals("유저 정보가 변경되었어요.", response.message)
    }

    @Test
    fun `프로필 수정 시 성별만 변경할 수 있다`() {
        val updated = user(Gender.FEMALE).apply { generatePublicId() }
        `when`(userService.update(any(), any(), any(), any(), any())).thenReturn(updated)

        useCase.updateUser(UpdateUserInfoRequest(name = null, phone = null, profileImage = null, gender = Gender.FEMALE))

        verify(userService).update(userId, null, null, null, Gender.FEMALE)
    }

    @Test
    fun `프로필 수정 시 전화번호를 보내지 않으면 전화번호 인증을 확인하지 않는다`() {
        val updated = user(Gender.MALE).apply { generatePublicId() }
        `when`(userService.update(any(), any(), any(), any(), any())).thenReturn(updated)

        useCase.updateUser(UpdateUserInfoRequest(name = "김도담", phone = null, profileImage = null, gender = null))

        verify(phoneVerificationStore, never()).ensureActive(any())
        verify(userService).update(userId, "김도담", null, null, null)
    }

    @Test
    fun `프로필 수정 시 전화번호를 바꾸면 전화번호 인증을 확인한다`() {
        val updated = user(Gender.MALE).apply { generatePublicId() }
        `when`(userService.update(any(), any(), any(), any(), any())).thenReturn(updated)

        useCase.updateUser(UpdateUserInfoRequest(name = null, phone = "01099998888", profileImage = null, gender = Gender.MALE))

        verify(phoneVerificationStore).ensureActive("01099998888")
    }

    @Test
    fun `프로필 수정 시 전화번호 인증이 되어 있지 않으면 수정하지 않는다`() {
        doThrow(PhoneNotVerifiedException())
            .`when`(phoneVerificationStore).ensureActive("01099998888")

        assertThrows(PhoneNotVerifiedException::class.java) {
            useCase.updateUser(
                UpdateUserInfoRequest(name = null, phone = "01099998888", profileImage = null, gender = Gender.FEMALE)
            )
        }

        verify(userService, never()).update(any(), any(), any(), any(), any())
        assertEquals(emptyList<Any>(), sentEvents(KafkaTopics.USER_UPDATED))
    }

    @Test
    fun `프로필 수정 시 전화번호가 중복이면 유저 변경 이벤트를 발행하지 않는다`() {
        doThrow(PhoneAlreadyExistsException())
            .`when`(userService).update(any(), any(), any(), any(), any())

        assertThrows(PhoneAlreadyExistsException::class.java) {
            useCase.updateUser(
                UpdateUserInfoRequest(name = null, phone = null, profileImage = null, gender = Gender.FEMALE)
            )
        }

        assertEquals(emptyList<Any>(), sentEvents(KafkaTopics.USER_UPDATED))
    }

    @Test
    fun `프로필 수정 후 유저 변경 이벤트가 발행된다`() {
        val updated = user(Gender.FEMALE).apply {
            generatePublicId()
            updateInfo("김도담", "01099998888", null, Gender.FEMALE)
        }
        `when`(userService.update(any(), any(), any(), any(), any())).thenReturn(updated)
        `when`(userService.getRoles(updated)).thenReturn(setOf(RoleType.STUDENT))

        useCase.updateUser(
            UpdateUserInfoRequest(name = "김도담", phone = "01099998888", profileImage = null, gender = Gender.FEMALE)
        )

        val event = sentEvents(KafkaTopics.USER_UPDATED).single() as UserUpdatedEvent
        assertEquals(updated.publicId, event.publicId)
        assertEquals("김도담", event.name)
        assertEquals("01099998888", event.phone)
        assertEquals(listOf(RoleType.STUDENT.name), event.roles.toList())
    }

    // ---------- helpers ----------

    private fun stubCreate() {
        `when`(userService.create(any(), any()))
            .thenAnswer { it.getArgument<UserEntity>(0).apply { generatePublicId() } }
    }

    /**
     * Mockito matcher 는 null 을 반환해 코틀린 null 검사에 걸리므로,
     * 넘겨진 인자는 mock 에 기록된 호출에서 직접 꺼낸다.
     */
    private fun createdUsers(service: UserService = userService): List<UserEntity> =
        invocations(service, "create").map { it.getArgument(0) }

    private fun createdRoles(service: UserService = userService): List<RoleType> =
        invocations(service, "create").map { it.getArgument(1) }

    private fun createdStudents(): List<StudentEntity> =
        invocations(studentService, "create").map { it.getArgument(0) }

    private fun createdTeachers(): List<TeacherEntity> =
        invocations(teacherService, "create").map { it.getArgument(0) }

    private fun sentEvents(topic: String): List<Any> =
        invocations(kafkaMessageProducer, "send")
            .filter { it.getArgument<String>(0) == topic }
            .map { it.getArgument(it.arguments.size - 1) }

    private fun invocations(target: Any, method: String) =
        mockingDetails(target).invocations.filter { it.method.name == method }

    private fun setCreatedAt(user: UserEntity) {
        val field = user.javaClass.superclass.getDeclaredField("createdAt")
        field.isAccessible = true
        field.set(user, LocalDateTime.of(2026, 8, 30, 9, 0))
    }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()

    private fun user(gender: Gender) = UserEntity(
        username = "dodam",
        name = "박준석",
        password = "password",
        phone = "01012345678",
        status = StatusType.PENDING,
        gender = gender,
    )

    private fun studentRequest(gender: Gender) = StudentRegisterRequest(
        username = "dodam",
        name = "박준석",
        password = "password",
        phone = "01012345678",
        grade = 2,
        room = 3,
        number = 11,
        gender = gender,
    )

    private fun teacherRequest(gender: Gender) = TeacherRegisterRequest(
        username = "teacher",
        name = "김선생",
        password = "password",
        phone = "01087654321",
        position = "담임",
        gender = gender,
    )
}
