plugins {
    application
}

application {
    mainClass.set("ServerKt")
}

dependencies {
    implementation(project(":common"))
}