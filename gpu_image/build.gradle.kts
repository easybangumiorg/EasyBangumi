
import com.heyanle.buildsrc.Android

plugins {
    alias(build.plugins.android.library)
    alias(build.plugins.kotlin.android)
//    id("com.android.library")
//    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heyanle.gpu_image"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        lint.targetSdk = Android.compileSdk

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(androidx.core.ktx)
    implementation(androidx.appcompat)
}