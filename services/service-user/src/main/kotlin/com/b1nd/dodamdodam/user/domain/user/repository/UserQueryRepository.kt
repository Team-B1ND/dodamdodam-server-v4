package com.b1nd.dodamdodam.user.domain.user.repository

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.user.command.UserSearchCommand
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import org.springframework.data.domain.Pageable

interface UserQueryRepository {
    fun searchUsers(keyword: String?, roles: List<RoleType>?, generationOnly: Boolean?, status: List<StatusType>?, pageable: Pageable): List<UserSearchCommand>
}