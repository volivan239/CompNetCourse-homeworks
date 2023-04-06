plugins {
    id("org.jetbrains.compose") version "1.3.1"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
}