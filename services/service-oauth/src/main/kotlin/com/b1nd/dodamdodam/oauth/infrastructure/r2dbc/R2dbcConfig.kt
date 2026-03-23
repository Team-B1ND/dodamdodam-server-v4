package com.b1nd.dodamdodam.oauth.infrastructure.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import java.util.UUID

@Configuration
class R2dbcConfig(private val connectionFactory: ConnectionFactory) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory = connectionFactory

    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        return R2dbcCustomConversions(storeConversions, listOf(
            UuidToStringConverter(),
            StringToUuidConverter(),
        ))
    }

    class UuidToStringConverter : Converter<UUID, String> {
        override fun convert(source: UUID): String = source.toString()
    }

    class StringToUuidConverter : Converter<String, UUID> {
        override fun convert(source: String): UUID = UUID.fromString(source)
    }
}
