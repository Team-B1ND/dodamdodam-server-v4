package com.b1nd.dodamdodam.user.domain.student.exception

import com.b1nd.dodamdodam.core.common.exception.BasicException

class StudentNotFoundException: BasicException(StudentExceptionCode.STUDENT_NOT_FOUND)
class ParentPhoneNotFoundException: BasicException(StudentExceptionCode.PARENT_PHONE_NOT_FOUND)