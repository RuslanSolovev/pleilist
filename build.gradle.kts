@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.0" apply false
}

buildscript {
    val kotlin_version = "1.9.0"

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0-beta02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        // Hilt плагин удалён
    }
}
