plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.detekt)
}

kotlin {
    explicitApi()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.android.jdk.get()
            }
        }
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jdk.get()
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
        tvosArm64(),
        tvosX64(),
        tvosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "datamap-runtime"
            isStatic = true
        }
        target.compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xbinary=bundleId=com.kkalisz.datamap.runtime")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib.common)
            }
        }

        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
            }
        }
    }
}

android {
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    namespace = group.toString()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.android.jdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.android.jdk.get())
    }
}
