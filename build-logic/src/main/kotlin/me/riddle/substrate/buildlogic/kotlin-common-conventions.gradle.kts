plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    testImplementation(platform(libs.findLibrary("junit.bom").get()))
    testImplementation(libs.findLibrary("junit.jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit.platform.launcher").get())
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
