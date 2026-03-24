package com.b1nd.dodamdodam.oauth.infrastructure.r2dbc

import com.b1nd.dodamdodam.oauth.infrastructure.r2dbc.converter.UUIDReadingConverter
import com.b1nd.dodamdodam.oauth.infrastructure.r2dbc.converter.UUIDWritingConverter
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions

@Configuration
class R2dbcConfig(private val connectionFactory: ConnectionFactory) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory = connectionFactory

    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        return R2dbcCustomConversions(storeConversions, listOf(
            UUIDWritingConverter(),
            UUIDReadingConverter(),
        ))
    }
}
