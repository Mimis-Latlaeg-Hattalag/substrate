import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(platform(substrate.serialization.bom))
    implementation(platform(substrate.slf4j.bom))

    implementation(substrate.serialization.json)

    api(substrate.slf4j.api)
    runtimeOnly(substrate.logback.classic)
    implementation(substrate.kotlin.logging.jvm)

    testImplementation(kotlin("test"))
    testImplementation(substrate.datetime.jvm)
}

tasks.test<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("me.riddle.substrate.examples.step00.SimpleMcpServerKt")
}


// Claude us packaged with system Java 15.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
