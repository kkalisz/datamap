import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt) apply true
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    parallel = true
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

subprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    tasks.register("detektAll") {
        group = "verification"
        allprojects {
            this@register.dependsOn(tasks.withType<Detekt>())
        }
    }

}
