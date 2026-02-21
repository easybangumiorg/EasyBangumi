import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
object ApplyHelper {

    // 三种表示方式。。
    val JAVA_TARGET = JvmTarget.JVM_21
    val JAVA_VERSION = JavaVersion.VERSION_21
    const val JAVA_VERSION_INT = 21

    fun applyKmp(
        target: Project,
        provider: EasyBuildConfigProvider,
    ) {
        target.extensions.configure<KotlinMultiplatformExtension> {


            androidTarget {
                compilerOptions {
                    jvmTarget.set(JAVA_TARGET)
                }

            }

            jvm("desktop") {
                compilerOptions {
                    jvmTarget.set(JAVA_TARGET)
                    freeCompilerArgs.add(
                        "-Xcontext-parameters"
                    )
                }

            }

            // 提前预埋保证 commonMain 是纯 kotlin 环境
            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }


            sourceSets.apply {
                val jvmMain = create("jvmMain") {
                    dependsOn(commonMain.get())
                }

                val iosMain = create("iosMain") {
                    dependsOn(commonMain.get())
                }

                val desktopMain = getByName("desktopMain") {
                    dependsOn(jvmMain)
                }

                androidMain.configure {
                    dependsOn(jvmMain)
                }

                iosArm64Main.configure {
                    dependsOn(iosMain)
                }
                iosX64Main.configure {
                    dependsOn(iosMain)
                }
                iosSimulatorArm64Main.configure {
                    dependsOn(iosMain)
                }
            }

            sourceSets.all {
                languageSettings.apply {
                    if (provider.optMd3Api) {
                        // 为所有 Material3 实验性 API 启用
                        optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                    }
                    optIn("kotlin.experimental.ExperimentalContextReceivers")
                    optIn("kotlin.time.ExperimentalTime")
                }
            }

        }
    }

    fun applyAndroidLibrary(
        target: Project,
        provider: EasyBuildConfigProvider
    ) {
        val androidCompileSdk = provider.findProperty("easy.build.androidCompileSdk").toIntOrNull()
        val androidMinSdk = provider.findProperty("easy.build.androidMinSdk").toIntOrNull()


        val name = target.path.replace(":", ".").removePrefix(".")
//        println("Applying Android plugin to project: $name with namespace: $configNamespace $name")
        if (name.isBlank()) {
            throw IllegalArgumentException("Project name cannot be blank.")
        }
        target.pluginManager.withPlugin("com.android.library") {
            target.extensions.configure<LibraryExtension> {
                namespace = "${provider.applicationId}.$name"
                compileSdk = androidCompileSdk

                defaultConfig {
                    minSdk = androidMinSdk
                    targetSdk = androidCompileSdk
                    lint {
                        targetSdk = androidCompileSdk
                    }
                }
                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }

                buildTypes {
                    debug {
                        isMinifyEnabled = false
                        isShrinkResources = false
                    }
                    release {
                        isMinifyEnabled = false
                        isShrinkResources = false
                    }
                }
            }
        }
    }

    fun applyAndroidApplication(
        target: Project,
        provider: EasyBuildConfigProvider
    ) {
        val androidCompileSdk = provider.findProperty("easy.build.androidCompileSdk").toIntOrNull()
        val androidMinSdk = provider.findProperty("easy.build.androidMinSdk").toIntOrNull()

        target.extensions.configure<BaseAppModuleExtension> {
            namespace =  provider.namespace
            compileSdk = androidCompileSdk
            defaultConfig {
                applicationId = provider.applicationId
                minSdk = androidMinSdk
                versionCode = provider.versionCode
                versionName = provider.versionName
                targetSdk = androidCompileSdk
                lint {
                    targetSdk = androidCompileSdk
                }
            }

            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }

            buildTypes {
                debug {
                    isMinifyEnabled = false
                    isShrinkResources = false
                }
                release {
                    isMinifyEnabled = false
                    isShrinkResources = false
                }
            }

        }
    }

    fun applyKotlinJar(
        project: Project,
        provider: EasyBuildConfigProvider
    ) {
        project.extensions.configure<JavaPluginExtension>() {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }

        project.extensions.configure<KotlinProjectExtension>() {
            jvmToolchain(JAVA_VERSION_INT)
        }



    }

}