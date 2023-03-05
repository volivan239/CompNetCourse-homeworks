plugins {
    application
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.2")
    implementation("com.sksamuel.hoplite:hoplite-json:2.7.2")
    implementation("com.sun.mail:javax.mail:1.6.2")
}