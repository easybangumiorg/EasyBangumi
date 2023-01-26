package com.heyanle.buildsrc

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created by HeYanLe on 2022/9/10 16:48.
 * https://github.com/heyanLE
 */
const val implementation = "implementation"
const val ksp = "ksp"
const val kapt = "kapt"
const val debugImplementation = "debugImplementation"
const val testImplementation = "testImplementation"
const val androidTestImplementation = "androidTestImplementation"

fun DependencyHandler.implementation(src: String) {
    add(implementation, src)
}

fun DependencyHandler.project(path: String): Dependency {
    return project(mapOf("path" to path))
}

fun DependencyHandler.ksp(src: String) {
    add(ksp, src)
}

fun DependencyHandler.kapt(src: String) {
    add(kapt, src)
}

fun DependencyHandler.debugImplementation(src: String) {
    add(debugImplementation, src)
}

fun DependencyHandler.testImplementation(src: String) {
    add(testImplementation, src)
}

fun DependencyHandler.androidTestImplementation(src: String) {
    add(androidTestImplementation, src)
}