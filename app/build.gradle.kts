@file:Suppress("UnstableApiUsage")
import com.android.build.api.dsl.VariantDimension
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.RoomSchemaArgProvider
import java.util.Properties

val DEFAULT_RELEASE = false
val release = isRelease()

fun isRelease() = (System.getenv("RELEASE") ?: "") == "true"

plugins {
    alias(build.plugins.android.application)
    alias(build.plugins.kotlin.android)
    alias(build.plugins.ksp)
    if ((System.getenv("RELEASE") ?: "true") == "") {
        id("com.google.gms.google-services")
        id("com.google.firebase.crashlytics")
    }
}



val publishingProps = Properties()
runCatching {
    publishingProps.load(project.rootProject.file("publishing/publishing.properties").inputStream())
}.onFailure {
    // it.printStackTrace()
}

val packageName = if (release) "com.heyanle.easybangumi4" else "com.heyanle.easybangumi4.debug"

android {
    namespace =  "com.heyanle.easybangumi4"
    compileSdk = Android.compileSdk

    defaultConfig {

        applicationId = packageName
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["bugly_appid"] =
            publishingProps.getProperty("bugly_appid", System.getenv("BUGLY_APPID")?:"")
        manifestPlaceholders["bugly_app_version"] = Android.versionName
        manifestPlaceholders["bugly_app_channel"] = "github"
        manifestPlaceholders["package_name"] = packageName
        manifestPlaceholders["is_release"] = release

        // bugly 调试模式
        manifestPlaceholders["bugly_is_debug"] = false

        ksp {
            arg("room.generateKotlin", "true")
            arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
        }

    }

//    splits {
//
//        abi {
//            isEnable = true
//            reset()
//            include("arm64-v8a", "armeabi-v7a")
//            isUniversalApk = true
//        }
//    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }


    packaging {
        resources.excludes.add("META-INF/beans.xml")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")

            buildConfig()

//            configure<CrashlyticsExtension> {
//                mappingFileUploadEnabled = false
//            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")

            buildConfig()

//            configure<CrashlyticsExtension> {
//                mappingFileUploadEnabled = false
//            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
        )
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = build.versions.compose.compiler.get()
    }

}

fun VariantDimension.buildConfig(){

//    // thanks
//    val donatezfb = project.rootProject.file("thanks_zfb.jpg")
//    val donatewx = project.rootProject.file("thanks_wx.png")
//
//    val zfbBase = com.heyanle.buildsrc.Base64Util.encodeImgageToBase64(donatezfb) ?: ""
//    val wxBase = com.heyanle.buildsrc.Base64Util.encodeImgageToBase64(donatewx) ?: ""
//
//    buildConfigField("String", "donateZfbBase64", "\"${zfbBase}\"")
//    buildConfigField("String", "wxBase", "\"${wxBase}\"")
//
//    val update = try {
//        // update log
//        val readMeFile = project.rootProject.file("README.md")
//        val stringBuilder = StringBuilder()
//        var isInUpdate = false
//        for (readLine in readMeFile.readLines()) {
//            if (readLine.startsWith("# 更新列表 ")){
//                isInUpdate = !isInUpdate
//                continue
//            }
//            if (isInUpdate){
//                stringBuilder.append(readLine.trim()).append("\\n")
//            }
//
//        }
//        stringBuilder.toString()
//    }catch (e: Throwable){
//        e.printStackTrace()
//        ""
//    }
//    buildConfigField("String", "updateLog", "\"${update}\"")

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(androidx.bundles.core)
    androidTestImplementation(androidx.bundles.test.core)

    implementation(androidx.bundles.room.impl)
    implementation(androidx.room.paging)
    annotationProcessor(androidx.room.compiler)
    ksp(androidx.room.compiler)
    testImplementation(androidx.room.testing)
    androidTestImplementation(androidx.room.testing)

    implementation(androidx.preference.ktx)

    implementation(androidx.medie)

    implementation(androidx.google.material)

    implementation(androidx.webkit)

    implementation(androidx.window)

    implementation(androidx.paging.common)
    implementation(androidx.paging.compose)
    implementation(androidx.paging.runtime.ktx)

    implementation(compose.bundles.ui)
    implementation(compose.bundles.runtime)
    implementation(compose.bundles.animation)
    implementation(compose.bundles.foundation)
    implementation(compose.bundles.material)
    implementation(compose.bundles.material3)

    implementation(libs.bundles.okhttp3)
    //implementation(libs.bundles.appcenter)

    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.moshi)

    //debugImplementation(libs.leakcanary)

    implementation(libs.glide)
    implementation(libs.okkv2)

    testImplementation(libs.junit)

//    implementation(libs.accompanist.systemuicontroller)
//    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.permissions)
    implementation(libs.navigtion.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.commons.text)
    implementation(libs.compose.reorderable)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.ktor.core)
    implementation(libs.ktor.android)

    // implementation(project(":easy-dlna"))
    implementation(project(":easy-crasher"))
    implementation(project(":easy-i18n"))
    implementation(project(":inject"))
    implementation(project(":lib_upnp"))
//    implementation(project(":gpu_image"))
    //implementation(project(":lib_signal"))

    implementation(libs.zip4j)

    implementation(extension.extension.api)

    implementation(libs.bugly)

    // fimplementation(gecko.gecko)

    implementation(libs.aria.m3u8)
    implementation(libs.aria.compiler)
    implementation(libs.aria)

    implementation(libs.jeff.m3u8)


    implementation(project(":EasyPlayer2:easyplayer2"))

    implementation(project(":EasyMediaTransformer:easy_transformer"))

    implementation(libs.uni.file)

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")


}