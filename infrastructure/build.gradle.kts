plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.mysqlDriver)
    implementation(libs.flywayCore)
    runtimeOnly(libs.flywayDatabaseMysql)

    implementation(libs.hikariCp)
    implementation(libs.javaJwt)

    implementation(libs.argon2Jvm)

    testImplementation(kotlin("test-junit5"))
}
