import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)

    alias(libs.plugins.venniktech)
}

val versions = Properties()
file("version.properties").inputStream().use { stream ->
    versions.load(stream)
}

group = "eu.gryta"
val library: String = "ktor.utils"
version = versions.getProperty("version")

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "$group.$library".replace(oldChar = '.', newChar = '-')
            isStatic = true
        }
    }

    jvm()
    //wasmJs()

    sourceSets {
        // Main
        commonMain.dependencies {
            api(libs.ktor.client.core)
        }
        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            api(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            api(libs.ktor.client.darwin)
        }
        // Test
        commonTest.dependencies {
            implementation(libs.bundles.ktor.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "$group.$library"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Add boot for ios device - patching missing certificates that cause test failure
val deviceName = project.findProperty("iosDevice") as? String ?: "iPhone 16"

tasks.register<Exec>("bootIOSSimulator") {
    isIgnoreExitValue = true
    errorOutput = System.err
    commandLine("xcrun", "simctl", "boot", deviceName)

    doLast {
        val result = executionResult.get()
        if (result.exitValue != 148 && result.exitValue != 149) { // ignoring device already booted errors
            result.assertNormalExitValue()
        }
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    if (Os.isFamily(Os.FAMILY_MAC)) {
        dependsOn("bootIOSSimulator")
        standalone.set(false)
        device.set(deviceName)
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/rgryta/Ktor-Utils")
            credentials {
                username =
                    project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
            }
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = library,
        version = versions.getProperty("version")
    )

    pom {
        name.set("KMP Library containing Ktor Utility pack")
        description.set("This library can be utilized by various KMP targets to build API Clients more easily")
        inceptionYear.set("2025")
        url.set("https://github.com/rgryta/Ktor-Utils")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("rgryta")
                name.set("Radosław Gryta")
                email.set("radek.gryta@gmail.com")
                url.set("https://github.com/rgryta/")
            }
        }

        scm {
            url.set("https://github.com/rgryta/Ktor-Utils")
            connection.set("scm:git:git://github.comrgryta/Ktor-Utils.git")
            developerConnection.set("scm:git:ssh://git@github.com/rgryta/Ktor-Utils.git")
        }
    }
}