rootProject.name = "ftp-client"
include("core")
include("cli-client")
include("gui-client")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
