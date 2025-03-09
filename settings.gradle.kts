rootProject.name = "datamap"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

include(":processor")
include(":runtime")
include(":sample")
