package com.b1nd.dodamdodam.oauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.b1nd.dodamdodam"])
@ConfigurationPropertiesScan
class OauthServiceApplication

fun main(args: Array<String>) {
    runApplication<OauthServiceApplication>(*args)
}
