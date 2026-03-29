package com.b1nd.dodamdodam.core.security.configuration

import org.springframework.security.crypto.password.PasswordEncoder
import java.security.MessageDigest

class Sha512PasswordEncoder : PasswordEncoder {

    override fun encode(rawPassword: CharSequence): String {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(rawPassword.toString().toByteArray())
        return md.digest().joinToString("") {
            ((it.toInt() and 0xff) + 0x100).toString(16).substring(1)
        }
    }

    override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
        return encodedPassword == encode(rawPassword)
    }
}
