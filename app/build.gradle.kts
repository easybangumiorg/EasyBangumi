
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.Config
import com.heyanle.buildsrc.RoomSchemaArgProvider
import com.heyanle.buildsrc.Version
import com.heyanle.buildsrc.androidTestImplementation
import com.heyanle.buildsrc.implementation
import com.heyanle.buildsrc.project

plugins {
    alias(build.plugins.android.application)
    alias(build.plugins.kotlin.android)
    alias(build.plugins.ksp)
}

android {
    namespace = "com.heyanle.easybangumi4"
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "com.heyanle.easybangumi4"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            Config.APP_CENTER_SECRET,
            "\"${Config.getPrivateValue(Config.APP_CENTER_SECRET)}\""
        )

        ksp {
            arg("room.generateKotlin", "true")
            arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
        }

    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }


    packaging {
        resources.excludes.add("META-INF/beans.xml")
    }

    buildTypes {
        release {
            postprocessing {
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                isObfuscate = false
                isOptimizeCode = true
                proguardFiles("proguard-rules.pro")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compiler.get()
    }

}



dependencies {

    implementation(androidx.bundles.core)
    androidTestImplementation (androidx.bundles.test.core)

    implementation(androidx.bundles.room.impl)
    implementation(androidx.room.paging)
    ksp(androidx.room.compiler)
    testImplementation(androidx.room.testing)
    androidTestImplementation(androidx.room.testing)

    implementation(androidx.medie)

    implementation(androidx.google.material)

    implementation(androidx.webkit)

    implementation(androidx.window)

    implementation(androidx.paging.compose)
    implementation(androidx.paging.runtime.ktx)

    implementation(libs.bundles.okhttp3)
    implementation(libs.bundles.cling)
    implementation(libs.bundles.appcenter)

    implementation(libs.jsoup)
    implementation(libs.gson)

    debugImplementation(libs.leakcanary)

    implementation(libs.glide)
    implementation(libs.okkv2)

    testImplementation(libs.junit)

    implementation(libs.easyplayer2)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.navigtion.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.commons.text)
    implementation(libs.bundles.cling)
    implementation(libs.compose.reorderable)

    implementation(project(":easy-dlna"))
    implementation(project(":easy-crasher"))
    implementation(project(":easy-i18n"))
    implementation(project(":injekt"))
    implementation(project(":extension:extension-core"))


    implementation(extension.extension.api)




}