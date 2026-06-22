plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "loli.ball.easyplayer2"
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
        freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
}

afterEvaluate {
    publishing {
        publications {
            create("maven_public", MavenPublication::class) {
                groupId = "loli.ball"
                artifactId = "easyplayer2"
                version = "1.0.0.test"
                from(components.getByName("release"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")

    val composeMaterialVersion = "1.6.8"
    implementation("androidx.compose.material:material:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-core:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeMaterialVersion")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3:1.2.1")

    val media3 = "1.4.0-alpha02"
    api("androidx.media3:media3-exoplayer:$media3")
    api("androidx.media3:media3-exoplayer-dash:$media3")
    api("androidx.media3:media3-ui:$media3")
    api("androidx.media3:media3-exoplayer-hls:$media3")
    api("androidx.media3:media3-transformer:$media3")
    api("androidx.media3:media3-common:$media3")
    api("androidx.media3:media3-effect:$media3")
    api("androidx.media3:media3-muxer:$media3")
}