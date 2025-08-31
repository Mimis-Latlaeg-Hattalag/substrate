plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    // Future private dependencies
}

application {
    mainClass = "me.riddle.substrate.core.app.AppKt"
}