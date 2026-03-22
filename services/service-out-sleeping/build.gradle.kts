plugins {
    id("buildsrc.convention.spring-boot-application")
}

dependencies {
    // core modules
    implementation(project(":core:core-common"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-jpa"))
    implementation(project(":core:core-grpc"))

    // grpc
    implementation(libs.springGrpc)

    // database
    runtimeOnly(libs.mysql.jdbcDriver)
    implementation(libs.springBootStarterData.jdbc)
    implementation(libs.springBootStarterData.jpa)

    // flyway
    implementation(libs.flywayCore)
    implementation(libs.flywayMysql)

    // swagger
    implementation(libs.springdoc.openapi.webmvc.ui)

    // test
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}
