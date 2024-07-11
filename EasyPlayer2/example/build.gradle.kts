@file:Suppress("UnstableApiUsage")
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

	namespace = "com.loli.ball.esayplayer2.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.loli.ball.esayplayer2.example"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}

dependencies {
    implementation(project(":easyplayer2"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.runtime:runtime:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.0")
}
