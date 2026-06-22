plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heyanle.easy_transformer"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        // freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}
gradle.projectsEvaluated {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=androidx.media3.common.util.UnstableApi"
    }
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")

    val media3 = "1.4.0-alpha02"
    api("androidx.media3:media3-exoplayer:$media3")
    api("androidx.media3:media3-exoplayer-dash:$media3")
    api("androidx.media3:media3-ui:$media3")
    api("androidx.media3:media3-exoplayer-hls:$media3")
    api("androidx.media3:media3-transformer:$media3")
    api("androidx.media3:media3-common:$media3")
    api("androidx.media3:media3-effect:$media3")
    api("androidx.media3:media3-muxer:$media3")

    implementation("androidx.annotation:annotation:1.8.0")
}