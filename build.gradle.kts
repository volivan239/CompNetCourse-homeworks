import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

application {
    mainClass.set("ServerKt")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("com.athaydes.rawhttp:rawhttp-core:2.5.2")
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.2")
    implementation("com.sksamuel.hoplite:hoplite-json:2.7.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}