plugins {
    id("buildsrc.convention.kotlin-jvm")
    `java-library`
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    api(project(":domain"))
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test-junit5"))
}

sourceSets {
    named("main") {
        kotlin {
            // Template-only files: reference ports not created yet.
            exclude("com/giftledger/application/services/ExportService.kt")
        }
    }
}
