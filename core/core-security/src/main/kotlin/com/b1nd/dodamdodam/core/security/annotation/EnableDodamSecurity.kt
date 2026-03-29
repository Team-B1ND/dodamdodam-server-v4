package com.b1nd.dodamdodam.core.security.annotation

import com.b1nd.dodamdodam.core.security.configuration.CoreSecurityAutoConfiguration
import com.b1nd.dodamdodam.core.security.configuration.ReactiveCoreSecurityAutoConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(CoreSecurityAutoConfiguration::class, ReactiveCoreSecurityAutoConfiguration::class)
annotation class EnableDodamSecurity
