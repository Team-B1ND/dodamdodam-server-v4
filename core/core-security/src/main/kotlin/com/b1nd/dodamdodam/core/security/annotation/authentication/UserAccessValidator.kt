package com.b1nd.dodamdodam.core.security.annotation.authentication

import com.b1nd.dodamdodam.core.security.exception.AccessDeniedException
import com.b1nd.dodamdodam.core.security.exception.UserDisabledException
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails

object UserAccessValidator {

    fun validate(authentication: Authentication?, userAccess: UserAccess) {
        val principal = authentication?.principal as? UserDetails

        if (userAccess.hasAnyRoleOnly && authentication?.authorities.isNullOrEmpty()) {
            throw AccessDeniedException()
        }

        if (userAccess.enabledOnly && principal?.isEnabled != true) {
            throw UserDisabledException()
        }

        val roles = userAccess.roles
        if (roles.isNotEmpty()) {
            val hasRole = roles.any { role ->
                authentication?.authorities?.any { it.authority == role.value } == true
            }
            if (!hasRole) throw AccessDeniedException()
        }
    }
}
