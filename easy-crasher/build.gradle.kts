
import com.heyanle.buildsrc.Android

plugins {
    alias(build.plugins.android.library)
    alias(build.plugins.kotlin.android)
//    id("com.android.library")
//    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heyanle.easy_crasher"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.compileSdk

        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(androidx.core.ktx)
    implementation(androidx.appcompat)
}