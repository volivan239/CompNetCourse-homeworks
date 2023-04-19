plugins {
    application
}

application {
    mainClass.set("ServerKt")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
}