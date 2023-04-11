plugins {
    application
}

application {
    mainClass.set("ServerKt")
}

dependencies {
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.2")
    implementation("com.sksamuel.hoplite:hoplite-json:2.7.2")
}