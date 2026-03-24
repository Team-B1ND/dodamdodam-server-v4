package com.b1nd.dodamdodam.core.security.passport

import com.b1nd.dodamdodam.core.security.exception.PassportExpiredException
import com.b1nd.dodamdodam.core.security.passport.crypto.PassportCompressor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

object PassportResolver {

    private const val DEFAULT_HEADER = "X-User-Passport"

    fun headerName(): String = DEFAULT_HEADER

    fun resolve(raw: String?): Authentication? {
        if (raw.isNullOrBlank()) return null

        val passport = decompress(raw)
        val userDetails = PassportUserDetails(passport)
        return UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
    }

    fun extractPassport(raw: String): Passport = decompress(raw)

    fun extractUserId(raw: String): java.util.UUID = decompress(raw).requireUserId()

    private fun decompress(raw: String): Passport {
        val passport = PassportCompressor.decompress(raw)
        if (passport.expiredAt < System.currentTimeMillis()) throw PassportExpiredException()
        return passport
    }
}
