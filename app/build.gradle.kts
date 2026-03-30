plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapters:http"))
    implementation(project(":adapters:persistence"))
    implementation(project(":infrastructure"))

    // Keep existing template module (optional, can remove later).
    implementation(project(":utils"))

    implementation(libs.ktorServerNetty)

    implementation(libs.koinCore)
    implementation(libs.koinKtor)
    implementation(libs.koinLoggerSlf4j)

    runtimeOnly(libs.logbackClassic)
}

application {
    mainClass = "com.giftledger.adapters.http.ApplicationKt"
}
