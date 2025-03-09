plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(project(":runtime"))
    ksp(project(":processor"))

    testImplementation(libs.kotlin.test.junit)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
