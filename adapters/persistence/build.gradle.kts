plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation(libs.bundles.exposed)
    implementation(libs.exposedJavaTime)
    runtimeOnly(libs.mysqlDriver)

    testImplementation(kotlin("test-junit5"))
}
