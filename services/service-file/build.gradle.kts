plugins {
    id("buildsrc.convention.spring-boot-application")
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-jpa"))
    implementation(project(":core:core-kafka"))
    implementation(project(":core:core-github-client"))

    implementation(project(":core:core-grpc"))
    implementation(libs.springGrpc)
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.15.3")

    // swagger
    implementation(libs.springdoc.openapi.webmvc.ui)

    // database
    runtimeOnly(libs.mysql.jdbcDriver)
    implementation(libs.springBootStarterData.jpa)
    implementation(libs.flywayCore)
    implementation(libs.flywayMysql)

    // aws s3
    implementation(platform("software.amazon.awssdk:bom:2.42.9"))
    implementation("software.amazon.awssdk:s3")
}
