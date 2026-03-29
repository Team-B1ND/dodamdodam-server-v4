package com.b1nd.dodamdodam.oauth.support

import java.security.MessageDigest

object TokenHashUtil {
    fun sha256(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
