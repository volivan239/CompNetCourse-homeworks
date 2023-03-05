plugins {
    id("java")
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}