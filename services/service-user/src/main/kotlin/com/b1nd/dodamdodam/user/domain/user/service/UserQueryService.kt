package com.b1nd.dodamdodam.user.domain.user.service

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.user.command.UserSearchPageCommand
import com.b1nd.dodamdodam.user.domain.user.repository.UserQueryRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserQueryService(
    private val userQueryRepository: UserQueryRepository
) {
    fun search(keyword: String?, roles: List<RoleType>?, generationOnly: Boolean?, pageable: Pageable): UserSearchPageCommand {
        val results = userQueryRepository.searchUsers(keyword, roles, generationOnly, pageable)
        val hasNext = results.size > pageable.pageSize
        return UserSearchPageCommand(
            content = if (hasNext) results.dropLast(1) else results,
            hasNext = hasNext,
        )
    }
}