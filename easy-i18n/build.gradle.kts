plugins {
//    id("com.android.library")
//    id("org.jetbrains.kotlin.android")
    alias(build.plugins.android.library)
    alias(build.plugins.kotlin.android)
}

android {
    namespace = "com.heyanle.easy_i18n"
    compileSdk = com.heyanle.buildsrc.Android.compileSdk

    defaultConfig {
        minSdk = com.heyanle.buildsrc.Android.minSdk
        lint.targetSdk = com.heyanle.buildsrc.Android.targetSdk
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
}