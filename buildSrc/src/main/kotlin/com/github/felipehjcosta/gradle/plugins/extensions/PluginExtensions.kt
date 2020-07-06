package com.github.felipehjcosta.gradle.plugins.extensions

import org.gradle.api.Project
import org.gradle.api.plugins.ObjectConfigurationAction

internal fun Project.applyAndroidApplicationPlugins() {
    apply {
        androidApplicationPlugin()
        kotlinAndroidPlugin()
        kotlinAndroidExtensionsPlugin()
        kotlinKaptPlugin()
    }
}

internal fun Project.applyAndroidDynamicFeaturePlugins() {
    apply {
        androidDynamicFeaturePlugin()
        kotlinAndroidPlugin()
        kotlinAndroidExtensionsPlugin()
        kotlinKaptPlugin()
    }
}

private fun ObjectConfigurationAction.androidDynamicFeaturePlugin() = plugin("com.android.dynamic-feature")

private fun ObjectConfigurationAction.androidApplicationPlugin() = plugin("com.android.application")

private fun ObjectConfigurationAction.kotlinAndroidPlugin() = plugin("kotlin-android")

private fun ObjectConfigurationAction.kotlinAndroidExtensionsPlugin() = plugin("kotlin-android-extensions")

private fun ObjectConfigurationAction.kotlinKaptPlugin() = plugin("kotlin-kapt")