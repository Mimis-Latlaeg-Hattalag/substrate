plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

    testImplementation(catalog.findLibrary("junit-jupiter").get())
    testRuntimeOnly(catalog.findLibrary("junit-platform-launcher").get())
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}