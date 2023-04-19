plugins {
    application
}

application {
    mainClass.set("ClientKt")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
}