import org.gradle.kotlin.dsl.internal.relocated.kotlin.metadata.internal.metadata.deserialization.VersionRequirementTable.Companion.create

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

            val kotlinSerializationVersion: String by settings

            val slf4jVersion: String by settings
            val logbackVersion: String by settings
            val kotlinLoggingVersion: String by settings

            version("serializationBOM",kotlinSerializationVersion)

            version("slf4jBOM", slf4jVersion)
            version("logback", logbackVersion)
            version("logging", kotlinLoggingVersion)

            library("serialization-bom", "org.jetbrains.kotlinx", "kotlinx-serialization-bom").versionRef("serializationBOM")

            library("slf4j-bom", "org.slf4j", "slf4j-bom").versionRef("slf4jBOM")
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")
            library("kotlin-logging", "io.github.oshai", "kotlin-logging").versionRef("logging")
            library("kotlin-logging-jvm", "io.github.oshai", "kotlin-logging-jvm").versionRef("logging")

        }

    }

}

rootProject.name = "substrate"

include("lib", "app")

// Progression examples - showing the evolution
include("examples:progression:step00_simplest_mcp")
// Future steps (uncomment as we build them):
// include("examples:progression:step01_with_memory")
// include("examples:progression:step02_with_persistence")
// include("examples:progression:step03_with_refusal")
