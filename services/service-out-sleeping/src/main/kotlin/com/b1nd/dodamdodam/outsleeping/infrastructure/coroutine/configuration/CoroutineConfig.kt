package com.b1nd.dodamdodam.outsleeping.infrastructure.coroutine.configuration

import com.b1nd.dodamdodam.core.common.coroutine.CoroutineBlockingExecutor
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class CoroutineConfig {
    @Bean(destroyMethod = "shutdown")
    fun coroutineExecutor(): ExecutorService =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
        )

    @Bean
    fun blockingExecutor(executor: ExecutorService) =
        CoroutineBlockingExecutor(executor.asCoroutineDispatcher())

    @Bean
    fun smsScope(executor: ExecutorService): CoroutineScope =
        CoroutineScope(
            SupervisorJob() + executor.asCoroutineDispatcher() + CoroutineExceptionHandler { _, ex ->
                LoggerFactory.getLogger(CoroutineConfig::class.java).error("문자 발송 코루틴 실패", ex)
            }
        )
}