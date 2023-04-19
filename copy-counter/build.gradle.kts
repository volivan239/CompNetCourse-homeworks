plugins {
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.compose") version "1.3.1"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

compose.desktop {
    application {
        mainClass = "AppKt"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.5.0")
    implementation(compose.desktop.currentOs)
}