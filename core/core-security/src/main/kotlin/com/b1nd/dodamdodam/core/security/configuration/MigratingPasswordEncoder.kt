package com.b1nd.dodamdodam.core.security.configuration

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class MigratingPasswordEncoder : PasswordEncoder {

    private val bcryptEncoder = BCryptPasswordEncoder()
    private val sha512Encoder = Sha512PasswordEncoder()

    override fun encode(rawPassword: CharSequence): String {
        return bcryptEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
        return if (isBcrypt(encodedPassword)) {
            bcryptEncoder.matches(rawPassword, encodedPassword)
        } else {
            sha512Encoder.matches(rawPassword, encodedPassword)
        }
    }

    private fun isBcrypt(encodedPassword: String): Boolean {
        return encodedPassword.startsWith("$2a$") ||
            encodedPassword.startsWith("$2b$") ||
            encodedPassword.startsWith("$2y$")
    }
}
