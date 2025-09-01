pluginManagement {

    val versionOfToolchainsFoojayResolver: String by extra
    val group: String by extra
    val version: String by extra
    val javaVersion: String by extra
    val kotlinVersion: String by extra

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version versionOfToolchainsFoojayResolver
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
    }

    logger.quiet("""
        
        Riddle me this: dependency resolution
        =====================================
        group:      $group
        version:    $version
        =====================================
        toolchain:  $versionOfToolchainsFoojayResolver
        java:       $javaVersion
        =====================================
        kotlin:     $kotlinVersion
        =====================================
        
    """.trimIndent())
}

dependencyResolutionManagement {

    versionCatalogs {
        create("substrate") {
            val kotlinXSerializationVersion: String by settings
            val kotlinXCoroutinesVersion: String by settings
            val kotlinXDateTimeVersion: String by settings

            val slf4jVersion: String by settings
            val logbackVersion: String by settings
            val kotlinLoggingVersion: String by settings

            version("serializationBOM",kotlinXSerializationVersion)
            version("coroutinesBOM",kotlinXCoroutinesVersion)
            version("datetime",kotlinXDateTimeVersion)

            version("slf4jBOM", slf4jVersion)
            version("logback", logbackVersion)
            version("logging", kotlinLoggingVersion)

            library("serialization-bom", "org.jetbrains.kotlinx", "kotlinx-serialization-bom").versionRef("serializationBOM")
            library("coroutines-bom", "org.jetbrains.kotlinx", "kotlinx-coroutines-bom").versionRef("coroutinesBOM")

            library("serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").withoutVersion()

            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").withoutVersion()

            library("slf4j-bom", "org.slf4j", "slf4j-bom").versionRef("slf4jBOM")
            library("slf4j-api", "org.slf4j", "slf4j-api").withoutVersion()
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
            library("kotlin-logging", "io.github.oshai", "kotlin-logging").versionRef("logging")
            library("kotlin-logging-jvm", "io.github.oshai", "kotlin-logging-jvm").versionRef("logging")

            library("datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").versionRef("datetime")
            library("datetime-jvm", "org.jetbrains.kotlinx", "kotlinx-datetime-jvm").versionRef("datetime")

        }
    }
}

rootProject.name = "substrate"

include("lib", "app")

include("examples:progression:step00_simplest_mcp")
include("examples:progression:step01_memory")
