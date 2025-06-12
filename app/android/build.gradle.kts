import com.android.build.gradle.tasks.MergeSourceSetFolders

plugins {
    alias(builds.plugins.androidApplication)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)
    alias(builds.plugins.ksp)
    id("AndroidAppResource")
    id("EasyLibBuild")
}



android {
    defaultConfig {
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    packaging {
        resources.excludes.add("META-INF/beans.xml")
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }


}


dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.preference.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.logback.android)


    implementation(projects.shared)


}


easyAndroidAssets {
    assetsDir = project.layout.projectDirectory.dir("../assets")
}