plugins {
    alias(builds.plugins.androidLibrary)
    alias(builds.plugins.kotlinAndroid)
}

android {
    namespace = AppConfig.namespace + ".utils.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        lint.targetSdk = 34

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    api(projects.utils.jvm)
}