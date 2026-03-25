package com.b1nd.dodamdodam.user.domain.user.command

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.admin.entity.AdminEntity
import com.b1nd.dodamdodam.user.domain.student.entity.StudentEntity
import com.b1nd.dodamdodam.user.domain.teacher.entity.TeacherEntity
import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity

data class UserSearchCommand(
    val user: UserEntity,
    val roles: Set<RoleType>,
    val student: StudentEntity?,
    val teacher: TeacherEntity?,
    val admin: AdminEntity?,
)