dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":domain")
include(":application")
include(":adapters:http")
include(":adapters:persistence")
include(":infrastructure")
include(":app")
include(":utils")

rootProject.name = "Gift_Ledger"
