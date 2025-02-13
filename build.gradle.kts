// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {


    dependencies {
        classpath ("com.android.tools.build:gradle:8.1.0") // Use the latest Android Gradle plugin

        classpath ("com.google.gms:google-services:4.4.0")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Using the Kotlin version variable
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}