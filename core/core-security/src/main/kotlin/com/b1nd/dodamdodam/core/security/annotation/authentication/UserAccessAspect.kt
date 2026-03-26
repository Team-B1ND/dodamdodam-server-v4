package com.b1nd.dodamdodam.core.security.annotation.authentication

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.core.context.SecurityContextHolder

@Aspect
class UserAccessAspect {

    @Around("@annotation(userAccess)")
    fun around(joinPoint: ProceedingJoinPoint, userAccess: UserAccess): Any? {
        UserAccessValidator.validate(SecurityContextHolder.getContext().authentication, userAccess)
        return joinPoint.proceed()
    }
}
