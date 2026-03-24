package com.b1nd.dodamdodam.oauth

import com.b1nd.dodamdodam.core.security.annotation.EnableDodamSecurity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.b1nd.dodamdodam"])
@ConfigurationPropertiesScan
@EnableDodamSecurity
class OauthServiceApplication

fun main(args: Array<String>) {
    runApplication<OauthServiceApplication>(*args)
}
