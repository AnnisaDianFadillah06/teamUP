// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Google Services Plugin (Latest Version)
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false

}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.44")

    }
}



// Clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}