pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Plugin version
            version("kotlin", "2.0.21")
            version("dokka", "1.9.20")
            version("detekt", "1.23.6")
            version("kover", "0.8.3")
            version("ktlint", "12.1.2")

            // Dependency version
            version("ktor", "3.0.1")
            version("kotlin-serialization", "1.6.3")
            version("kotlin-coroutine", "1.9.0")
            version("slf4j", "2.0.16")

            plugin("kt-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kt-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("dokka", "org.jetbrains.dokka").versionRef("dokka")
            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").versionRef("ktlint")
            plugin("kover", "org.jetbrains.kotlinx.kover").versionRef("kover")

            library("ktor-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
            library("ktor-logging", "io.ktor", "ktor-client-logging").versionRef("ktor")
            library("ktor-serialization", "io.ktor", "ktor-client-serialization").versionRef("ktor")
            library("ktor-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            library("kt-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlin-serialization")

            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")

            // Test
            library("kt-coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test").versionRef("kotlin-coroutine")

            bundle(
                "ktor",
                listOf("ktor-core", "ktor-serialization", "ktor-content-negotiation", "ktor-serialization-json"),
            )
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "mojang-api"
