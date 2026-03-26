package com.b1nd.dodamdodam.oauth.infrastructure.r2dbc.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.util.UUID

@WritingConverter
class UUIDWritingConverter : Converter<UUID, String> {
    override fun convert(source: UUID): String = source.toString()
}

@ReadingConverter
class UUIDReadingConverter : Converter<String, UUID> {
    override fun convert(source: String): UUID = UUID.fromString(source)
}
