package com.b1nd.dodamdodam.oauth.support

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.GZIPInputStream

object PassportParser {

    private val objectMapper = jacksonObjectMapper()

    fun extractUserPublicId(passportHeader: String): String {
        val compressed = Base64.getDecoder().decode(passportHeader)
        val json = GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader().use { it.readText() }
        val map: Map<String, Any?> = objectMapper.readValue(json)
        return map["userId"]?.toString() ?: throw IllegalStateException("userId not found in passport")
    }
}
