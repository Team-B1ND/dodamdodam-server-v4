plugins {
    id("buildsrc.convention.spring-boot-application")
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-grpc"))
    implementation(libs.springGrpc)

    // webflux (reactive)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.springBootStarterData.r2dbc)
    runtimeOnly(libs.mysql.r2dbcDriver)

    // flyway (jdbc 필요)
    implementation(libs.flywayCore)
    implementation(libs.flywayMysql)
    implementation(libs.mysql.jdbcDriver)

    // jwt
    implementation(libs.jwt.nimbusJose)

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")

    // crypto
    implementation("org.springframework.security:spring-security-crypto")
}
