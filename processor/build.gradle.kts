plugins {
    kotlin("jvm")
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(project(":runtime"))

    implementation(libs.ksp.api)

    implementation(libs.ksp.aa.embeddable)
    testImplementation(libs.ksp.commonDeps)
    testImplementation(libs.ksp.cli)

    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.ksp.api)
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
}
