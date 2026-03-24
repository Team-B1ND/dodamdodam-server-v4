package com.b1nd.dodamdodam.core.security.configuration

import com.b1nd.dodamdodam.core.security.filter.ReactivePassportFilter
import com.b1nd.dodamdodam.core.security.filter.ReactiveUserAccessFilter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class ReactiveCoreSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun reactivePassportFilter() = ReactivePassportFilter()

    @Bean
    fun reactiveUserAccessFilter(
        @Qualifier("requestMappingHandlerMapping") handlerMapping: RequestMappingHandlerMapping,
    ) = ReactiveUserAccessFilter(handlerMapping)
}
