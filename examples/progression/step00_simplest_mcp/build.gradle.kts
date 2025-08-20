plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(gradleApi())
    implementation(platform(kotlin("bom")))
    implementation(platform(substrate.serialization.bom))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
}


application {
    mainClass.set("me.riddle.substrate.examples.step01.SimpleMcpServerKt")
}
