plugins {
    id("buildsrc.convention.spring-boot-application")
    kotlin("kapt")
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-jpa"))
    implementation(project(":core:core-grpc"))
    implementation(libs.springGrpc)

    // database
    runtimeOnly(libs.mysql.jdbcDriver)
    implementation(libs.springBootStarterData.jpa)
    implementation(libs.springdoc.openapi.webmvc.ui)

    // querydsl
    implementation(libs.querydsl.jpa) { artifact { classifier = "jakarta" } }
    kapt(libs.querydsl.apt) { artifact { classifier = "jakarta" } }

    // flyway
    implementation(libs.flywayCore)
    implementation(libs.flywayMysql)
}