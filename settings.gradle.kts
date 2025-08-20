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
            version("serializationBOM", kotlinSerializationVersion)

            library("serialization-bom", "org.jetbrains.kotlinx", "kotlinx-serialization-bom").versionRef("serializationBOM")
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
