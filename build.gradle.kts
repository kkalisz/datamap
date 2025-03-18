import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt) apply true
    alias(libs.plugins.vanniktech.mavenPublish) apply false
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
val modulesToPublish = listOf("runtime", "processor")

subprojects {
    if (name in modulesToPublish) {
        afterEvaluate {
            plugins.withId(
                libs.plugins.vanniktech.mavenPublish
                    .get()
                    .pluginId,
            ) {
                configure<MavenPublishBaseExtension> {
                    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)

                    signAllPublications()

                    coordinates(group.toString(), name, version.toString())

                    pom {
                        name = this@subprojects.name
                        description = "A Kotlin Symbol Processing (KSP) plugin that generates builder classes for Kotlin data classes."
                        inceptionYear = "2025"
                        url = "https://github.com/kkalisz/datamap"
                        licenses {
                            license {
                                name.set("Apache License 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }

                        scm {
                            url.set("https://github.com/kkalisz/datamap")
                            connection.set("scm:git:git://github.com/kkalisz/datamap.git")
                        }

                        developers {
                            developer {
                                id.set("kkalisz")
                                name.set("Kamil Kalisz (kkalisz)")
                                url.set("https://github.com/kkalisz")
                            }
                        }
                    }
                }
            }
        }
    }
}
