import com.heyanle.buildsrc.*

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


dependencies {
    coroutines()
    okhttp3()
    jsoup()
    gson()
    roomCommon()
}
