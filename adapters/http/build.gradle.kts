plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure"))
    implementation(project(":adapters:persistence"))
    implementation(libs.kotlinxSerialization)

    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
    implementation(libs.ktorServerAuth)
    implementation(libs.ktorServerAuthJwt)
    implementation(libs.ktorServerCors)
    implementation(libs.ktorServerStatusPages)

    implementation(libs.koinKtor)
    implementation(libs.koinLoggerSlf4j)
    implementation(libs.ktorSwaggerUi)

    // Exposed for database connection
    implementation(libs.exposedCore)
    implementation(libs.exposedJdbc)

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.kotestRunnerJunit5)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestProperty)
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.7")
}

sourceSets {
    named("main") {
        kotlin {
            // Template-only file: references types not created yet.
            exclude("com/giftledger/adapters/http/routes/AdvancedRoutes.kt")
        }
    }
}
